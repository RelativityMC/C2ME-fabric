package com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Parses the build-time prelude module so generated code can be merged into it.
 *
 * <p>Vulkan forbids {@code OpCapability Linkage}, so the prelude and the runtime-generated
 * density function code must occupy a single self-contained module. This splits the prelude
 * into SPIR-V logical-layout sections, recovers the {@code <id>}s of the types, constants and
 * extended instruction sets it already declares -- re-declaring any of those would be invalid
 * -- and locates the stubbed {@code df_binding_*} functions by their {@code OpName}.</p>
 */
public class SpirVPrelude {

    private final int version;
    private final int idBound;

    private final Map<SpirVModule.Section, IntArrayList> sections = new EnumMap<>(SpirVModule.Section.class);

    private final Object2IntOpenHashMap<String> typeKeys = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<String> constantKeys = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<String> extInstImports = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<String> namedIds = new Object2IntOpenHashMap<>();
    private final Int2IntOpenHashMap specIds = new Int2IntOpenHashMap();
    private final Int2ObjectOpenHashMap<String> idTypeKeys = new Int2ObjectOpenHashMap<>();
    private final List<FunctionRange> functions = new ArrayList<>();
    /**
     * Function {@code <id>} to its declared parameter types, in order.
     */
    private final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<int[]> functionParamTypes =
            new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>();
    /**
     * Decorations the prelude already carries, so generated code does not repeat one.
     */
    private final java.util.HashSet<String> decorationKeys = new java.util.HashSet<>();

    private SpirVPrelude(int[] words) {
        if (words.length < 5) {
            throw new IllegalArgumentException("SPIR-V module too short: " + words.length + " words");
        }
        if (words[0] != SpirV.MAGIC) {
            throw new IllegalArgumentException(String.format("Not a SPIR-V module: magic 0x%08X", words[0]));
        }
        this.version = words[1];
        this.idBound = words[3];

        for (SpirVModule.Section section : SpirVModule.Section.values()) {
            this.sections.put(section, new IntArrayList());
        }
        this.typeKeys.defaultReturnValue(0);
        this.constantKeys.defaultReturnValue(0);
        this.extInstImports.defaultReturnValue(0);
        this.namedIds.defaultReturnValue(0);
        this.specIds.defaultReturnValue(-1);

        parse(words);
    }

    public static SpirVPrelude read(int[] words) {
        return new SpirVPrelude(words);
    }

    public static SpirVPrelude read(InputStream in) throws IOException {
        return new SpirVPrelude(toWords(in.readAllBytes()));
    }

    /**
     * SPIR-V is a stream of 32-bit words whose byte order is whatever produced it; the magic
     * number disambiguates.
     */
    public static int[] toWords(byte[] bytes) {
        if ((bytes.length & 3) != 0) {
            throw new IllegalArgumentException("SPIR-V byte length not a multiple of 4: " + bytes.length);
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        if (buffer.getInt(0) != SpirV.MAGIC) {
            buffer.order(ByteOrder.BIG_ENDIAN);
        }
        int[] words = new int[bytes.length / 4];
        for (int i = 0; i < words.length; i++) words[i] = buffer.getInt(i * 4);
        return words;
    }

    // ------------------------------------------------------------------
    // parsing
    // ------------------------------------------------------------------

    private static SpirVModule.Section sectionOf(int opcode) {
        if (opcode >= SpirV.OP_TYPE_FIRST && opcode <= SpirV.OP_TYPE_LAST) {
            return SpirVModule.Section.TYPES_CONSTANTS_GLOBALS;
        }
        return switch (opcode) {
            case SpirV.OP_CAPABILITY -> SpirVModule.Section.CAPABILITIES;
            case SpirV.OP_EXTENSION -> SpirVModule.Section.EXTENSIONS;
            case SpirV.OP_EXT_INST_IMPORT -> SpirVModule.Section.EXT_INST_IMPORTS;
            case SpirV.OP_MEMORY_MODEL -> SpirVModule.Section.MEMORY_MODEL;
            case SpirV.OP_ENTRY_POINT -> SpirVModule.Section.ENTRY_POINTS;
            case SpirV.OP_EXECUTION_MODE, SpirV.OP_EXECUTION_MODE_ID -> SpirVModule.Section.EXECUTION_MODES;
            case SpirV.OP_STRING, SpirV.OP_SOURCE, SpirV.OP_SOURCE_CONTINUED, SpirV.OP_SOURCE_EXTENSION,
                 SpirV.OP_NAME, SpirV.OP_MEMBER_NAME, SpirV.OP_MODULE_PROCESSED -> SpirVModule.Section.DEBUG_NAMES;
            case SpirV.OP_DECORATE, SpirV.OP_MEMBER_DECORATE, SpirV.OP_DECORATION_GROUP,
                 SpirV.OP_GROUP_DECORATE, SpirV.OP_GROUP_MEMBER_DECORATE, SpirV.OP_DECORATE_ID,
                 SpirV.OP_DECORATE_STRING, SpirV.OP_MEMBER_DECORATE_STRING -> SpirVModule.Section.ANNOTATIONS;
            default -> SpirVModule.Section.TYPES_CONSTANTS_GLOBALS;
        };
    }

    private static String readString(int[] words, int offset) {
        StringBuilder builder = new StringBuilder();
        outer:
        for (int i = offset; i < words.length; i++) {
            int word = words[i];
            for (int b = 0; b < 4; b++) {
                int c = (word >>> (b * 8)) & 0xFF;
                if (c == 0) break outer;
                builder.append((char) c);
            }
        }
        return new String(builder.toString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    /**
     * glslang mangles function OpNames with their parameter types --
     * {@code "df_binding_final_density(struct-sample_int32_ctx_t-u641-...;"} -- so the
     * declared name is everything before the first parenthesis.
     */
    private static String demangle(String name) {
        int paren = name.indexOf('(');
        return paren < 0 ? name : name.substring(0, paren);
    }

    private static long readLong(int[] words, int offset) {
        return (words[offset] & 0xFFFFFFFFL) | ((long) words[offset + 1] << 32);
    }

    private static String arrayToString(int[] words, int offset, int count) {
        int[] slice = new int[count];
        System.arraycopy(words, offset, slice, 0, count);
        return java.util.Arrays.toString(slice);
    }

    private void parse(int[] words) {
        int i = 5;
        // Function bodies span several instructions, so track the enclosing OpFunction.
        int functionStartWord = -1;
        int functionResultId = 0;
        int functionTypeId = 0;
        boolean functionHasBody = false;
        IntArrayList pendingFunction = null;
        it.unimi.dsi.fastutil.ints.IntOpenHashSet pendingDefinedIds = null;
        IntArrayList pendingParamTypes = null;

        while (i < words.length) {
            int header = words[i];
            int wordCount = header >>> 16;
            int opcode = header & 0xFFFF;
            if (wordCount == 0) {
                throw new IllegalArgumentException("Zero-length SPIR-V instruction at word " + i);
            }

            if (opcode == SpirV.OP_FUNCTION) {
                pendingFunction = new IntArrayList();
                pendingDefinedIds = new it.unimi.dsi.fastutil.ints.IntOpenHashSet();
                pendingParamTypes = new IntArrayList();
                functionStartWord = i;
                // OpFunction: <result type> <result id> <function control> <function type>
                functionResultId = words[i + 2];
                functionTypeId = words[i + 4];
                functionHasBody = false;
            }

            if (pendingFunction != null) {
                for (int w = 0; w < wordCount; w++) pendingFunction.add(words[i + w]);
                if (opcode == SpirV.OP_LABEL) functionHasBody = true;

                switch (opcode) {
                    case SpirV.OP_FUNCTION_PARAMETER -> {
                        pendingDefinedIds.add(words[i + 2]);
                        pendingParamTypes.add(words[i + 1]);
                    }
                    case SpirV.OP_VARIABLE -> pendingDefinedIds.add(words[i + 2]);
                    case SpirV.OP_LABEL -> pendingDefinedIds.add(words[i + 1]);
                    default -> {
                    }
                }

                if (opcode == SpirV.OP_FUNCTION_END) {
                    SpirVModule.Section target = functionHasBody
                            ? SpirVModule.Section.FUNCTION_DEFINITIONS
                            : SpirVModule.Section.FUNCTION_DECLARATIONS;
                    IntArrayList out = this.sections.get(target);
                    int startInSection = out.size();
                    out.addAll(pendingFunction);
                    this.functions.add(new FunctionRange(
                            functionResultId, functionTypeId, target, startInSection, out.size(), functionHasBody,
                            pendingDefinedIds));
                    this.functionParamTypes.put(functionResultId, pendingParamTypes.toIntArray());
                    pendingFunction = null;
                    pendingDefinedIds = null;
                    pendingParamTypes = null;
                    functionStartWord = -1;
                }
                i += wordCount;
                continue;
            }

            record(words, i, opcode, wordCount);
            i += wordCount;
        }

        if (pendingFunction != null) {
            throw new IllegalArgumentException("Unterminated OpFunction at word " + functionStartWord);
        }
    }

    private void record(int[] words, int offset, int opcode, int wordCount) {
        SpirVModule.Section section = sectionOf(opcode);
        IntArrayList out = this.sections.get(section);
        for (int w = 0; w < wordCount; w++) out.add(words[offset + w]);

        switch (opcode) {
            case SpirV.OP_EXT_INST_IMPORT -> this.extInstImports.put(readString(words, offset + 2), words[offset + 1]);
            case SpirV.OP_NAME -> this.namedIds.put(demangle(readString(words, offset + 2)), words[offset + 1]);
            case SpirV.OP_DECORATE -> {
                if (wordCount >= 4 && words[offset + 2] == SpirV.DECORATION_SPEC_ID) {
                    this.specIds.put(words[offset + 1], words[offset + 3]);
                }
                this.decorationKeys.add(words[offset + 1] + ":" + words[offset + 2]);
            }
            case SpirV.OP_MEMBER_DECORATE ->
                    this.decorationKeys.add(words[offset + 1] + ":" + words[offset + 2] + ":" + words[offset + 3]);
            default -> {
                if (opcode >= SpirV.OP_TYPE_FIRST && opcode <= SpirV.OP_TYPE_LAST) {
                    recordType(words, offset, opcode, wordCount);
                } else {
                    recordConstant(words, offset, opcode, wordCount);
                }
            }
        }
    }

    /**
     * Reconstructs the key {@link SpirVModule} would use, so the two caches agree.
     */
    private void recordType(int[] words, int offset, int opcode, int wordCount) {
        int id = words[offset + 1];
        String key = switch (opcode) {
            case SpirV.OP_TYPE_VOID -> "void";
            case SpirV.OP_TYPE_BOOL -> "bool";
            case SpirV.OP_TYPE_INT -> "i" + words[offset + 2] + (words[offset + 3] != 0 ? "s" : "u");
            case SpirV.OP_TYPE_FLOAT -> "f" + words[offset + 2];
            case SpirV.OP_TYPE_VECTOR -> "v" + words[offset + 2] + "x" + words[offset + 3];
            case SpirV.OP_TYPE_ARRAY -> "arr" + words[offset + 2] + "[" + words[offset + 3] + "]";
            case SpirV.OP_TYPE_RUNTIME_ARRAY -> "rtarr" + words[offset + 2];
            case SpirV.OP_TYPE_STRUCT -> "struct" + arrayToString(words, offset + 2, wordCount - 2);
            case SpirV.OP_TYPE_POINTER -> "ptr" + words[offset + 2] + "->" + words[offset + 3];
            case SpirV.OP_TYPE_FUNCTION -> "fn" + words[offset + 2] + arrayToString(words, offset + 3, wordCount - 3);
            // Types the backend never emits (images, samplers, forward pointers) are copied
            // through but left out of the cache; there is nothing to collide with.
            default -> null;
        };
        if (key != null) {
            this.typeKeys.put(key, id);
            this.idTypeKeys.put(id, key);
        }
    }

    private void recordConstant(int[] words, int offset, int opcode, int wordCount) {
        switch (opcode) {
            case SpirV.OP_CONSTANT_TRUE -> this.constantKeys.put("btrue", words[offset + 2]);
            case SpirV.OP_CONSTANT_FALSE -> this.constantKeys.put("bfalse", words[offset + 2]);
            case SpirV.OP_CONSTANT -> {
                int resultType = words[offset + 1];
                int id = words[offset + 2];
                String typeKey = this.idTypeKeys.get(resultType);
                if (typeKey == null) return;
                String key = switch (typeKey) {
                    case "i32s" -> "i32:" + words[offset + 3];
                    case "i32u" -> "u32:" + words[offset + 3];
                    case "i64s" -> "i64:" + readLong(words, offset + 3);
                    case "i64u" -> "u64:" + readLong(words, offset + 3);
                    case "f32" -> "f32:" + words[offset + 3];
                    case "f64" -> "f64:" + readLong(words, offset + 3);
                    default -> null;
                };
                if (key != null) this.constantKeys.put(key, id);
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------
    // merging
    // ------------------------------------------------------------------

    /**
     * Copies this prelude into {@code module}: its version, its word sections, its declared
     * types/constants/imports, and an id reservation so generated {@code <id>}s start above
     * the prelude's bound.
     */
    public void seedInto(SpirVModule module) {
        module.setVersion(this.version);
        module.reserveIds(this.idBound);
        for (Map.Entry<SpirVModule.Section, IntArrayList> entry : this.sections.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            // Function bodies are held back for seedFunctions: a stub the generated code
            // replaces reuses the prelude's <id>, so copying its body here too would define
            // that <id> twice.
            // Debug names and annotations are held back alongside the bodies: a dropped stub's
            // parameters and labels cease to exist, and a name left pointing at them dangles.
            if (entry.getKey() == SpirVModule.Section.FUNCTION_DECLARATIONS
                    || entry.getKey() == SpirVModule.Section.FUNCTION_DEFINITIONS
                    || entry.getKey() == SpirVModule.Section.DEBUG_NAMES
                    || entry.getKey() == SpirVModule.Section.ANNOTATIONS) {
                continue;
            }
            module.seedSection(entry.getKey(), entry.getValue().toIntArray());
        }
        this.decorationKeys.forEach(module::seedDecoration);
        this.typeKeys.forEach(module::seedType);
        this.constantKeys.forEach(module::seedConstant);
        this.extInstImports.forEach(module::seedExtInstImport);
    }

    /**
     * Copies the prelude's function declarations and definitions into {@code module}, omitting
     * every function whose {@code <id>} appears in {@code overriddenIds} -- those are the stubs
     * the generated code defines itself, under the same {@code <id>} so existing call sites bind
     * to it. Must run before generated function bodies are appended.
     */
    public void seedFunctions(SpirVModule module, it.unimi.dsi.fastutil.ints.IntSet overriddenIds) {
        it.unimi.dsi.fastutil.ints.IntOpenHashSet droppedIds = new it.unimi.dsi.fastutil.ints.IntOpenHashSet();
        for (FunctionRange range : this.functions) {
            // The function's own <id> survives -- generated code redefines it -- but everything
            // its body declared goes away with it.
            if (overriddenIds.contains(range.resultId())) droppedIds.addAll(range.definedIds());
        }
        seedFiltered(module, SpirVModule.Section.DEBUG_NAMES, droppedIds);
        seedFiltered(module, SpirVModule.Section.ANNOTATIONS, droppedIds);

        for (SpirVModule.Section section : new SpirVModule.Section[]{
                SpirVModule.Section.FUNCTION_DECLARATIONS, SpirVModule.Section.FUNCTION_DEFINITIONS}) {
            IntArrayList words = this.sections.get(section);
            if (words == null || words.isEmpty()) continue;

            boolean[] skip = new boolean[words.size()];
            for (FunctionRange range : this.functions) {
                if (range.section() != section || !overriddenIds.contains(range.resultId())) continue;
                for (int i = range.start(); i < range.end(); i++) skip[i] = true;
            }

            IntArrayList kept = new IntArrayList(words.size());
            for (int i = 0; i < words.size(); i++) {
                if (!skip[i]) kept.add(words.getInt(i));
            }

            // OpFunction is <header> <result type> <result id> <function control> <function type>,
            // so index 3 of the copied range is the control word.
            for (FunctionRange range : this.functions) {
                if (range.section() != section || overriddenIds.contains(range.resultId())) continue;
                if (!isNoInline(range.resultId())) continue;
                int offset = 0;
                for (int i = 0; i < range.start(); i++) {
                    if (!skip[i]) offset++;
                }
                if (offset + 3 < kept.size()) {
                    kept.set(offset + 3, SpirV.FUNCTION_CONTROL_DONT_INLINE);
                }
            }
            if (!kept.isEmpty()) module.seedSection(section, kept.toIntArray());
        }
    }

    /**
     * Whether the prelude declared this function {@code _noinline}, mirroring the OpenCL
     * backend's {@code FUNC_NOINLINE}. Those are the perlin octave samplers: leaf functions
     * reached from a thousand call sites, whose inlining is what makes the driver's compile
     * time explode. Everything else is left for the driver to inline as it sees fit.
     */
    private boolean isNoInline(int functionId) {
        for (Object2IntMap.Entry<String> entry : this.namedIds.object2IntEntrySet()) {
            if (entry.getIntValue() == functionId && entry.getKey().endsWith("_noinline")) return true;
        }
        return false;
    }

    /**
     * Seeds {@code section}, dropping every instruction whose target -- always the first operand
     * for the name and decoration opcodes glslang emits -- is an id that no longer exists.
     */
    private void seedFiltered(SpirVModule module, SpirVModule.Section section,
                              it.unimi.dsi.fastutil.ints.IntSet droppedIds) {
        IntArrayList words = this.sections.get(section);
        if (words == null || words.isEmpty()) return;

        IntArrayList kept = new IntArrayList(words.size());
        int i = 0;
        while (i < words.size()) {
            int wordCount = words.getInt(i) >>> 16;
            if (wordCount == 0) throw new IllegalStateException("Zero-length instruction in " + section);
            if (wordCount < 2 || !droppedIds.contains(words.getInt(i + 1))) {
                for (int w = 0; w < wordCount; w++) kept.add(words.getInt(i + w));
            }
            i += wordCount;
        }
        if (!kept.isEmpty()) module.seedSection(section, kept.toIntArray());
    }

    // ------------------------------------------------------------------
    // accessors
    // ------------------------------------------------------------------

    public int version() {
        return this.version;
    }

    public int idBound() {
        return this.idBound;
    }

    /**
     * {@code <id>} of a function or variable the prelude gave an {@code OpName}, or 0.
     *
     * <p>Names are demangled, so pass {@code "df_binding_final_density"} rather than glslang's
     * parameter-mangled form. A function the entry point never reaches is eliminated before
     * this sees it and will not be found.</p>
     */
    public int idOfName(String name) {
        return this.namedIds.getInt(name);
    }

    /**
     * The {@code SpecId} the prelude assigned to a {@code layout(constant_id = N)} declaration,
     * or -1. Looked up by name so generated code need not hardcode the numbering.
     */
    public int specIdOfName(String name) {
        int id = this.namedIds.getInt(name);
        return id == 0 ? -1 : this.specIds.get(id);
    }

    public Object2IntOpenHashMap<String> namedIds() {
        return this.namedIds;
    }

    public List<FunctionRange> functions() {
        return this.functions;
    }

    /**
     * Parameter types the prelude declared for {@code functionId}, or null if it declares none.
     */
    public int[] parameterTypesOf(int functionId) {
        return this.functionParamTypes.get(functionId);
    }

    /**
     * The function type {@code <id>} the prelude gave {@code functionId}, or 0.
     */
    public int functionTypeOf(int functionId) {
        for (FunctionRange range : this.functions) {
            if (range.resultId() == functionId) return range.typeId();
        }
        return 0;
    }

    /**
     * The type a {@code Function}-storage pointer points at, or 0 when {@code typeId} is not one.
     *
     * <p>glslang passes a parameter it writes to inside the body by pointer and one it only reads
     * by value, so a generated definition or call has to follow whatever the prelude declared.</p>
     */
    public int functionPointerPointee(int typeId) {
        String key = this.idTypeKeys.get(typeId);
        if (key == null || !key.startsWith("ptr")) return 0;
        int arrow = key.indexOf("->");
        if (arrow < 0) return 0;
        int storageClass = Integer.parseInt(key.substring(3, arrow));
        if (storageClass != SpirV.STORAGE_CLASS_FUNCTION) return 0;
        return Integer.parseInt(key.substring(arrow + 2));
    }

    public IntArrayList section(SpirVModule.Section section) {
        return this.sections.get(section);
    }

    /**
     * Where one of the prelude's functions lives, so a stubbed {@code df_binding_*} body can be
     * replaced with generated code. {@code start} and {@code end} index into the section's word
     * list, not the original module.
     */
    /**
     * @param definedIds ids this function's body defines. glslang only ever attaches an
     *                   {@code OpName} to a function, a parameter, a label or a local variable,
     *                   so tracking those four is enough to find the debug names that would
     *                   dangle if the function is dropped.
     */
    public record FunctionRange(int resultId, int typeId, SpirVModule.Section section, int start, int end,
                                boolean hasBody, it.unimi.dsi.fastutil.ints.IntOpenHashSet definedIds) {
    }

}
