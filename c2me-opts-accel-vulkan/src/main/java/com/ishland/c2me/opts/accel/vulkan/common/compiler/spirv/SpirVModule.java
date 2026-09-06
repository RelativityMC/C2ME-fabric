package com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builds a SPIR-V module word stream.
 *
 * <p>Instructions are appended to one of the {@link Section}s, which are concatenated in
 * SPIR-V logical layout order (specification 2.4) at {@link #toWords()}. Types and constants
 * are deduplicated, as the specification requires them to be unique.</p>
 *
 * <p>{@code <id>}s are allocated from {@link #nextId()}. When merging with a prelude compiled
 * at build time, seed the counter past the prelude's id bound with {@link #reserveIds(int)}
 * so the two id spaces do not collide.</p>
 */
public class SpirVModule {

    private final IntArrayList[] sections = new IntArrayList[Section.values().length];
    private final Object2IntOpenHashMap<String> typeCache = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<String> constantCache = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<String> extInstImports = new Object2IntOpenHashMap<>();
    /**
     * {@code target:decoration} keys already emitted, including the prelude's.
     */
    private final java.util.HashSet<String> decorations = new java.util.HashSet<>();
    private int version = SpirV.VERSION_1_5;
    private int nextId = 1;
    private boolean debugNames = false;
    public SpirVModule() {
        for (int i = 0; i < this.sections.length; i++) this.sections[i] = new IntArrayList();
        this.typeCache.defaultReturnValue(0);
        this.constantCache.defaultReturnValue(0);
        this.extInstImports.defaultReturnValue(0);
    }

    /**
     * Encodes a SPIR-V literal string: UTF-8, NUL-terminated, zero-padded to a word boundary.
     */
    public static int[] literalString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        int words = (bytes.length / 4) + 1;
        int[] out = new int[words];
        for (int i = 0; i < bytes.length; i++) {
            out[i >> 2] |= (bytes[i] & 0xFF) << ((i & 3) * 8);
        }
        return out;
    }

    private static int[] concat(int[] head, int[] tail) {
        int[] out = Arrays.copyOf(head, head.length + tail.length);
        System.arraycopy(tail, 0, out, head.length, tail.length);
        return out;
    }

    /**
     * Match the prelude's SPIR-V version when merging, so the header stays consistent.
     */
    public void setVersion(int version) {
        this.version = version;
    }

    // ------------------------------------------------------------------
    // ids
    // ------------------------------------------------------------------

    public void setDebugNames(boolean debugNames) {
        this.debugNames = debugNames;
    }

    public int nextId() {
        return this.nextId++;
    }

    /**
     * Moves the id counter to {@code bound}, so ids below it stay owned by the prelude.
     */
    public void reserveIds(int bound) {
        if (bound > this.nextId) this.nextId = bound;
    }

    // ------------------------------------------------------------------
    // raw emission
    // ------------------------------------------------------------------

    public int idBound() {
        return this.nextId;
    }

    /**
     * Emits an instruction with no result {@code <id>}.
     */
    public void emit(Section section, int opcode, int... operands) {
        IntArrayList out = this.sections[section.ordinal()];
        out.add(((operands.length + 1) << 16) | opcode);
        for (int operand : operands) out.add(operand);
    }

    /**
     * Emits an instruction whose first two operands are a result type and a freshly allocated
     * result {@code <id>}.
     *
     * @return the result {@code <id>}
     */
    public int emitResult(Section section, int opcode, int resultType, int... operands) {
        int result = nextId();
        IntArrayList out = this.sections[section.ordinal()];
        out.add(((operands.length + 3) << 16) | opcode);
        out.add(resultType);
        out.add(result);
        for (int operand : operands) out.add(operand);
        return result;
    }

    /**
     * As {@link #emitResult}, but for instructions with a result {@code <id>} and no result type.
     */
    public int emitResultNoType(Section section, int opcode, int... operands) {
        int result = nextId();
        IntArrayList out = this.sections[section.ordinal()];
        out.add(((operands.length + 2) << 16) | opcode);
        out.add(result);
        for (int operand : operands) out.add(operand);
        return result;
    }

    public void capability(int capability) {
        emit(Section.CAPABILITIES, SpirV.OP_CAPABILITY, capability);
    }

    // ------------------------------------------------------------------
    // preamble
    // ------------------------------------------------------------------

    public void extension(String name) {
        emit(Section.EXTENSIONS, SpirV.OP_EXTENSION, literalString(name));
    }

    public int extInstImport(String name) {
        int cached = this.extInstImports.getInt(name);
        if (cached != 0) return cached;
        int result = nextId();
        int[] operands = concat(new int[]{result}, literalString(name));
        IntArrayList out = this.sections[Section.EXT_INST_IMPORTS.ordinal()];
        out.add(((operands.length + 1) << 16) | SpirV.OP_EXT_INST_IMPORT);
        for (int operand : operands) out.add(operand);
        this.extInstImports.put(name, result);
        return result;
    }

    public void memoryModel(int addressingModel, int memoryModel) {
        emit(Section.MEMORY_MODEL, SpirV.OP_MEMORY_MODEL, addressingModel, memoryModel);
    }

    public void entryPoint(int executionModel, int entryPointId, String name, int... interfaceIds) {
        int[] operands = concat(new int[]{executionModel, entryPointId}, literalString(name));
        operands = concat(operands, interfaceIds);
        emit(Section.ENTRY_POINTS, SpirV.OP_ENTRY_POINT, operands);
    }

    public void executionModeLocalSize(int entryPointId, int x, int y, int z) {
        emit(Section.EXECUTION_MODES, SpirV.OP_EXECUTION_MODE, entryPointId, SpirV.EXECUTION_MODE_LOCAL_SIZE, x, y, z);
    }

    public void debugName(int target, String name) {
        if (!this.debugNames) return;
        emit(Section.DEBUG_NAMES, SpirV.OP_NAME, concat(new int[]{target}, literalString(name)));
    }

    public void decorate(int target, int decoration, int... operands) {
        if (!this.decorations.add(target + ":" + decoration)) return;
        emit(Section.ANNOTATIONS, SpirV.OP_DECORATE, concat(new int[]{target, decoration}, operands));
    }

    public void memberDecorate(int structType, int member, int decoration, int... operands) {
        if (!this.decorations.add(structType + ":" + member + ":" + decoration)) return;
        emit(Section.ANNOTATIONS, SpirV.OP_MEMBER_DECORATE, concat(new int[]{structType, member, decoration}, operands));
    }

    /**
     * Records a decoration the prelude already carries. Types are deduplicated against the
     * prelude's, so generated code can ask for one the prelude declared and decorated -- and a
     * type must not be decorated with the same decoration twice.
     */
    public void seedDecoration(String key) {
        this.decorations.add(key);
    }

    private int type(String key, int opcode, int... operands) {
        int cached = this.typeCache.getInt(key);
        if (cached != 0) return cached;
        int result = emitResultNoType(Section.TYPES_CONSTANTS_GLOBALS, opcode, operands);
        this.typeCache.put(key, result);
        return result;
    }

    // ------------------------------------------------------------------
    // types (deduplicated -- the specification requires uniqueness)
    // ------------------------------------------------------------------

    public int typeVoid() {
        return type("void", SpirV.OP_TYPE_VOID);
    }

    public int typeBool() {
        return type("bool", SpirV.OP_TYPE_BOOL);
    }

    public int typeInt(int width, boolean signed) {
        return type("i" + width + (signed ? "s" : "u"), SpirV.OP_TYPE_INT, width, signed ? 1 : 0);
    }

    public int typeFloat(int width) {
        return type("f" + width, SpirV.OP_TYPE_FLOAT, width);
    }

    public int typeVector(int componentType, int count) {
        return type("v" + componentType + "x" + count, SpirV.OP_TYPE_VECTOR, componentType, count);
    }

    public int typeArray(int elementType, int lengthConstantId) {
        return type("arr" + elementType + "[" + lengthConstantId + "]", SpirV.OP_TYPE_ARRAY, elementType, lengthConstantId);
    }

    public int typeRuntimeArray(int elementType) {
        return type("rtarr" + elementType, SpirV.OP_TYPE_RUNTIME_ARRAY, elementType);
    }

    public int typeStruct(int... memberTypes) {
        return type("struct" + Arrays.toString(memberTypes), SpirV.OP_TYPE_STRUCT, memberTypes);
    }

    public int typePointer(int storageClass, int pointeeType) {
        return type("ptr" + storageClass + "->" + pointeeType, SpirV.OP_TYPE_POINTER, storageClass, pointeeType);
    }

    public int typeFunction(int returnType, int... parameterTypes) {
        return type("fn" + returnType + Arrays.toString(parameterTypes), SpirV.OP_TYPE_FUNCTION,
                concat(new int[]{returnType}, parameterTypes));
    }

    private int constant(String key, int opcode, int resultType, int... literals) {
        int cached = this.constantCache.getInt(key);
        if (cached != 0) return cached;
        int result = emitResult(Section.TYPES_CONSTANTS_GLOBALS, opcode, resultType, literals);
        this.constantCache.put(key, result);
        return result;
    }

    // ------------------------------------------------------------------
    // constants (deduplicated)
    // ------------------------------------------------------------------

    public int constBool(boolean value) {
        String key = "b" + value;
        int cached = this.constantCache.getInt(key);
        if (cached != 0) return cached;
        int result = emitResult(Section.TYPES_CONSTANTS_GLOBALS,
                value ? SpirV.OP_CONSTANT_TRUE : SpirV.OP_CONSTANT_FALSE, typeBool());
        this.constantCache.put(key, result);
        return result;
    }

    public int constI32(int value) {
        return constant("i32:" + value, SpirV.OP_CONSTANT, typeInt(32, true), value);
    }

    public int constU32(int value) {
        return constant("u32:" + value, SpirV.OP_CONSTANT, typeInt(32, false), value);
    }

    /**
     * 64-bit literals occupy two words, low-order first.
     */
    public int constU64(long value) {
        return constant("u64:" + value, SpirV.OP_CONSTANT, typeInt(64, false),
                (int) value, (int) (value >>> 32));
    }

    public int constI64(long value) {
        return constant("i64:" + value, SpirV.OP_CONSTANT, typeInt(64, true),
                (int) value, (int) (value >>> 32));
    }

    public int constF32(float value) {
        return constant("f32:" + Float.floatToRawIntBits(value), SpirV.OP_CONSTANT, typeFloat(32),
                Float.floatToRawIntBits(value));
    }

    public int constF64(double value) {
        long bits = Double.doubleToRawLongBits(value);
        return constant("f64:" + bits, SpirV.OP_CONSTANT, typeFloat(64),
                (int) bits, (int) (bits >>> 32));
    }

    /**
     * Appends already-encoded words, for a caller that buffered a whole function.
     */
    public void appendWords(Section section, IntArrayList words) {
        this.sections[section.ordinal()].addAll(words);
    }

    /**
     * Registers an {@code <id>} the prelude already declared, so a later request for the same
     * type reuses it instead of emitting a duplicate (which the specification forbids).
     */
    void seedType(String key, int id) {
        this.typeCache.put(key, id);
    }

    // ------------------------------------------------------------------
    // merge seeding
    // ------------------------------------------------------------------

    void seedConstant(String key, int id) {
        this.constantCache.put(key, id);
    }

    void seedExtInstImport(String name, int id) {
        this.extInstImports.put(name, id);
    }

    /**
     * Prepends the prelude's words to a section, ahead of anything generated.
     */
    void seedSection(Section section, int[] words) {
        IntArrayList out = this.sections[section.ordinal()];
        IntArrayList merged = new IntArrayList(words.length + out.size());
        for (int word : words) merged.add(word);
        merged.addAll(out);
        this.sections[section.ordinal()] = merged;
    }

    public int[] toWords() {
        int size = 5;
        for (IntArrayList section : this.sections) size += section.size();

        int[] out = new int[size];
        out[0] = SpirV.MAGIC;
        out[1] = this.version;
        out[2] = SpirV.GENERATOR;
        out[3] = this.nextId; // id bound: ids are [1, bound)
        out[4] = 0;           // reserved schema

        int offset = 5;
        for (IntArrayList section : this.sections) {
            for (int i = 0; i < section.size(); i++) out[offset++] = section.getInt(i);
        }
        return out;
    }

    // ------------------------------------------------------------------
    // serialization
    // ------------------------------------------------------------------

    /**
     * The raw words of one section, for a merger that splices sections individually.
     */
    public int[] sectionWords(Section section) {
        return this.sections[section.ordinal()].toIntArray();
    }

    public List<int[]> allSections() {
        List<int[]> out = new ArrayList<>(this.sections.length);
        for (IntArrayList section : this.sections) out.add(section.toIntArray());
        return out;
    }

    /**
     * SPIR-V logical layout order.
     */
    public enum Section {
        CAPABILITIES,
        EXTENSIONS,
        EXT_INST_IMPORTS,
        MEMORY_MODEL,
        ENTRY_POINTS,
        EXECUTION_MODES,
        DEBUG_NAMES,
        ANNOTATIONS,
        TYPES_CONSTANTS_GLOBALS,
        FUNCTION_DECLARATIONS,
        FUNCTION_DEFINITIONS,
    }

}
