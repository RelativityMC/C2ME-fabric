package com.ishland.c2me.opts.accel.vulkan.common.compiler;

import com.ishland.c2me.base.mixin.access.IDoublePerlinNoiseSampler;
import com.ishland.c2me.base.mixin.access.IMultiNoiseBiomeSource;
import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilEntries;
import com.ishland.c2me.opts.accel.vulkan.common.Config;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirV;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirVGenFunctionContextImpl;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirVModule;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv.SpirVPrelude;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNodeLike;
import com.ishland.c2me.opts.dfc.common.ast.opto.OptoPasses;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenContext;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;
import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.SPV;
import org.lwjgl.util.shaderc.SPVBinary;
import org.lwjgl.util.shaderc.SPVOptimizer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Compiles a world's density function graph to SPIR-V.
 *
 * <p>Mirrors {@code OpenCLCGen}, with text emission replaced by direct SPIR-V. Vulkan forbids
 * {@code OpCapability Linkage}, so instead of linking against the prelude this loads the
 * build-time prelude module and generates into it: functions the prelude stubbed out
 * ({@code df_binding_*}) are re-emitted under the prelude's own {@code <id>}s, so every call
 * site already in the prelude resolves to generated code.</p>
 */
public class SpirVGen {

    /**
     * One prelude per program type; see the compilePreludeSpirV tasks.
     */
    public static final String PRELUDE_RESOURCE_FORMAT = "shaders/c2me_vulkan_ext_math_%s.spv";
    public static final Object MARKER_localOffsetTable = new Object(); // unused
    public static final Object MARKER_estimateSurfaceHeightCache = new Object();
    public static final Object MARKER_aquifer = new Object();
    public static final Object MARKER_fluidLevelSampler = new Object();
    public static final Object MARKER_oreVeinRandom = new Object();
    public static final Object MARKER_cacheLike_interpolator = new Object();
    public static final Object MARKER_cacheLike_flatCache = new Object();
    public static final Object MARKER_cacheLike_cache2d = new Object();
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SpirVGen.class);
    /**
     * Mirrors MASK_enableFlatCache / MASK_enableAllCaches in the prelude.
     */
    private static final int MASK_ENABLE_FLAT_CACHE = 1 << 0;
    private static final int MASK_ENABLE_ALL_CACHES = (1 << 1) | MASK_ENABLE_FLAT_CACHE;

    private static final AtomicLong ordinal = new AtomicLong();

    static {
        SpirVEmitterRegistry.init();
    }

    public static GeneratedSpirVSource compile(NoiseRouter noiseRouter,
                                               GenerationShapeConfig generationShapeConfig,
                                               Reference2ReferenceMap<DensityFunction, OptoPasses.AstPair> optoCache,
                                               DensityFunction finalFinalDensity,
                                               BiomeSource biomeSource) {
        // The same density function graph is emitted into each prelude variant. Generation is
        // deterministic, so the constant data and offset table come out identical; the metadata
        // from the last pass is therefore representative of all of them.
        EnumMap<ProgramType, int[]> modules = new EnumMap<>(ProgramType.class);
        ContextImpl context = null;

        for (ProgramType type : ProgramType.values()) {
            context = new ContextImpl(loadPrelude(type));
            context.prependConstants(generationShapeConfig);

            Object2ObjectLinkedOpenHashMap<String, OptoPasses.AstPair> dfs = new Object2ObjectLinkedOpenHashMap<>();
            dfs.put("barrier", optimizeCached(noiseRouter.barrierNoise(), optoCache));
            dfs.put("fluid_level_floodedness", optimizeCached(noiseRouter.fluidLevelFloodednessNoise(), optoCache));
            dfs.put("fluid_level_spread", optimizeCached(noiseRouter.fluidLevelSpreadNoise(), optoCache));
            dfs.put("lava", optimizeCached(noiseRouter.lavaNoise(), optoCache));
            dfs.put("temperature", optimizeCached(noiseRouter.temperature(), optoCache));
            dfs.put("vegetation", optimizeCached(noiseRouter.vegetation(), optoCache));
            dfs.put("continents", optimizeCached(noiseRouter.continents(), optoCache));
            dfs.put("erosion", optimizeCached(noiseRouter.erosion(), optoCache));
            dfs.put("depth", optimizeCached(noiseRouter.depth(), optoCache));
            dfs.put("ridges", optimizeCached(noiseRouter.ridges(), optoCache));
            dfs.put("preliminary_surface_level", optimizeCached(noiseRouter.preliminarySurfaceLevel(), optoCache));
            dfs.put("final_density", optimizeCached(noiseRouter.finalDensity(), optoCache));
            dfs.put("vein_toggle", optimizeCached(noiseRouter.veinToggle(), optoCache));
            dfs.put("vein_ridged", optimizeCached(noiseRouter.veinRidged(), optoCache));
            dfs.put("vein_gap", optimizeCached(noiseRouter.veinGap(), optoCache));
            dfs.put("final_final_density", optimizeCached(finalFinalDensity, optoCache));

            for (Map.Entry<String, OptoPasses.AstPair> entry : dfs.entrySet()) {
                context.compileBinding(entry.getValue().optimized(), entry.getKey());
            }

            context.genNoiseKernels(type);
            context.genBiomeTree(biomeSource);
            int[] words = optimize(context.buildModule());
            modules.put(type, words);
        }

        return context.buildSource(modules);
    }

    /**
     * Runs the generated module through SPIRV-Tools' optimiser.
     *
     * <p>Worth doing even though the driver optimises too: the module handed to it is the
     * prelude merged with code assembled instruction by instruction at world load, which leaves
     * far more redundancy -- loads and stores through Function variables, dead branches from the
     * cache-variant dispatchers -- than a compiler would emit. Measured on the noise kernel this
     * is the difference between 1.6ms and 0.3ms of device time.</p>
     *
     * <p>Failure is never fatal: the unoptimised module is valid on its own, so any problem here
     * costs performance rather than correctness.</p>
     */
    private static int[] optimize(int[] words) {
        if (!Config.optimizeGeneratedSpirV) return words;

        long optimizer = 0L;
        long options = 0L;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            optimizer = SPVOptimizer.spvOptimizerCreate(SPV.SPV_ENV_VULKAN_1_2);
            if (optimizer == 0L) return words;

            SPVOptimizer.spvOptimizerSetMessageConsumer(optimizer,
                    (level, source, position, message) ->
                            LOGGER.warn("SPIR-V optimiser [{}]: {}", level,
                                    MemoryUtil.memUTF8Safe(message)));
            SPVOptimizer.spvOptimizerRegisterPerformancePasses(optimizer);

            options = SPV.spvOptimizerOptionsCreate();
            // Specialization constants carry the per-world generation shape and are set when the
            // pipeline is created, so they must survive as specialization constants.
            SPV.spvOptimizerOptionsSetPreserveSpecConstants(options, true);
            // The module is validated separately; running the validator here just makes a
            // failure indistinguishable from a rejected optimisation.
            SPV.spvOptimizerOptionsSetRunValidator(options, false);

            IntBuffer input = MemoryUtil.memAllocInt(words.length);
            try {
                input.put(words).flip();
                // Returns spv_result_t, so SPV_SUCCESS is zero; the module comes back as an
                // spv_binary rather than through the return value.
                PointerBuffer output = stack.mallocPointer(1);
                int status = SPVOptimizer.spvOptimizerRun(optimizer, input, output, options);
                if (status != SPV.SPV_SUCCESS || output.get(0) == 0L) {
                    LOGGER.warn("SPIR-V optimisation failed ({}); using the module as generated", status);
                    return words;
                }

                SPVBinary binary = SPVBinary.create(output.get(0));
                int[] result = new int[(int) binary.wordCount()];
                binary.code().get(result);
                LOGGER.debug("Optimised module: {} -> {} words", words.length, result.length);
                return result;
            } finally {
                MemoryUtil.memFree(input);
            }
        } catch (Throwable t) {
            LOGGER.warn("SPIR-V optimisation unavailable; using the module as generated", t);
            return words;
        } finally {
            if (options != 0L) SPV.spvOptimizerOptionsDestroy(options);
            if (optimizer != 0L) SPVOptimizer.spvOptimizerDestroy(optimizer);
        }
    }

    private static SpirVPrelude loadPrelude(ProgramType type) {
        String resource = String.format(PRELUDE_RESOURCE_FORMAT, type.name().toLowerCase(java.util.Locale.ROOT));
        try (InputStream in = SpirVGen.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) throw new IllegalStateException("Prelude not found on classpath: " + resource);
            return SpirVPrelude.read(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SPIR-V prelude", e);
        }
    }

    private static OptoPasses.AstPair optimizeCached(DensityFunction densityFunction,
                                                     Reference2ReferenceMap<DensityFunction, OptoPasses.AstPair> optoCache) {
        return optoCache.computeIfAbsent(densityFunction,
                (DensityFunction df) -> OptoPasses.optimizeOCL(McToAst.toAst(df)));
    }

    private static void validateNodeType(AstNode node, AstNode.ReturnType returnType) {
        if (node.getReturnType() != returnType) {
            throw new IllegalArgumentException("Invalid descriptor: tried to store %s into %s"
                    .formatted(node.getReturnType(), returnType));
        }
    }

    private static void validateTarget(ValuesMethodDef target, AstNode.ReturnType returnType) {
        if (target.returnType() != returnType) {
            throw new IllegalArgumentException("Invalid descriptor: tried to store %s into %s"
                    .formatted(target.returnType(), returnType));
        }
    }

    private static ValuesMethodDef makeValuesMethodDef(String name, AstNode.ReturnType returnType) {
        return switch (returnType) {
            case F64 -> new ValuesMethodDefF64(name);
            case F32 -> new ValuesMethodDefF32(name);
        };
    }

    public static byte[] bytes(DoublePerlinNoiseSampler sampler) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment memorySegment = BindingsTemplate.double_octave_sampler_data$create(
                    arena,
                    ((IDoublePerlinNoiseSampler) sampler).getFirstSampler(),
                    ((IDoublePerlinNoiseSampler) sampler).getSecondSampler(),
                    ((IDoublePerlinNoiseSampler) sampler).getAmplitude(),
                    true
            );
            byte[] bytes = new byte[(int) memorySegment.byteSize()];
            MemorySegment.copy(memorySegment, ValueLayout.JAVA_BYTE, 0, bytes, 0, bytes.length);
            return bytes;
        }
    }

    public static byte[] bytes(InterpolatedNoiseSampler sampler) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment memorySegment = BindingsTemplate.interpolated_noise_sampler$create(arena, sampler, true);
            byte[] bytes = new byte[(int) memorySegment.byteSize()];
            MemorySegment.copy(memorySegment, ValueLayout.JAVA_BYTE, 0, bytes, 0, bytes.length);
            return bytes;
        }
    }

    public static byte[] bytes(int[] ints) {
        byte[] bytes = new byte[ints.length * Integer.BYTES];
        MemorySegment.copy(MemorySegment.ofArray(ints), ValueLayout.JAVA_BYTE, 0, bytes, 0, bytes.length);
        return bytes;
    }

    public static byte[] bytes(float[] floats) {
        byte[] bytes = new byte[floats.length * Float.BYTES];
        MemorySegment.copy(MemorySegment.ofArray(floats), ValueLayout.JAVA_BYTE, 0, bytes, 0, bytes.length);
        return bytes;
    }

    public static byte[] bytesObject(Object object) {
        return switch (object) {
            case InterpolatedNoiseSampler sampler -> bytes(sampler);
            case DoublePerlinNoiseSampler sampler -> bytes(sampler);
            case int[] ints -> bytes(ints);
            case float[] floats -> bytes(floats);
            default -> throw new UnsupportedOperationException(object.getClass().getName());
        };
    }

    public enum ProgramType {
        ESTIMATE_SURFACE_HEIGHT,
        AQUIFER_PREFILL,
        NOISE_KERNEL,
        FLAT_CACHE_PREFILL,
        CACHE2D_PREFILL,
        INTERPOLATOR_PREFILL,
        BIOME_MULTINOISE_KERNEL,
        ;
    }

    private record FunctionKey(AstNode node, SpirVGenFunctionContext.FunctionVariant variant) {
    }

    // ==================================================================
    // context
    // ==================================================================

    public static class ContextImpl implements SpirVGenContext {

        private final SpirVPrelude prelude;
        private final SpirVModule module = new SpirVModule();

        private final Object2ReferenceOpenHashMap<FunctionKey, String> methods = new Object2ReferenceOpenHashMap<>();
        private final Object2IntOpenHashMap<String> functionIds = new Object2IntOpenHashMap<>();
        private final List<SpirVGenFunctionContextImpl> pendingFunctions = new ArrayList<>();

        private final Reference2IntLinkedOpenHashMap<Object> globalDynamicDataOffsets = new Reference2IntLinkedOpenHashMap<>();
        private final Object2IntLinkedOpenHashMap<CacheLikeNode> flatCaches = new Object2IntLinkedOpenHashMap<>();
        private final Object2IntLinkedOpenHashMap<CacheLikeNode> interpolators = new Object2IntLinkedOpenHashMap<>();
        private final Object2IntLinkedOpenHashMap<CacheLikeNode> cache2ds = new Object2IntLinkedOpenHashMap<>();
        private final Object2IntOpenHashMap<String> specializationConstants = new Object2IntOpenHashMap<>();
        private final Object2IntOpenCustomHashMap<byte[]> globalConstDataOffsets = new Object2IntOpenCustomHashMap<>(new Hash.Strategy<>() {
            @Override
            public int hashCode(byte[] o) {
                return Arrays.hashCode(o);
            }

            @Override
            public boolean equals(byte[] a, byte[] b) {
                return Arrays.equals(a, b);
            }
        });
        /**
         * Ids taken over from the prelude; these are the entry points into generated code.
         */
        private final it.unimi.dsi.fastutil.ints.IntOpenHashSet preludeBackedFunctions =
                new it.unimi.dsi.fastutil.ints.IntOpenHashSet();
        private final it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap bufferReferences = new it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap();
        private final int localOffsetTableOffset;
        private RegistryEntry<Biome>[] biomeMappings = null;
        /**
         * Set once genNoiseKernels has emitted the prefills; no cache may be added after.
         */
        private boolean cacheFrozen = false;
        private int globalConstDataTail = 16;
        private int methodIdx = 0;

        public ContextImpl(SpirVPrelude prelude) {
            this.prelude = prelude;
            this.functionIds.defaultReturnValue(0);
            this.bufferReferences.defaultReturnValue(0);
            this.globalDynamicDataOffsets.defaultReturnValue(Integer.MAX_VALUE);
            this.flatCaches.defaultReturnValue(-1);
            this.interpolators.defaultReturnValue(-1);
            this.cache2ds.defaultReturnValue(-1);

            prelude.seedInto(this.module);

            this.localOffsetTableOffset = this.allocGlobalDynamicData(MARKER_localOffsetTable); // always zero
            this.allocGlobalDynamicData(DensityFunctionTypes.Beardifier.INSTANCE);
            this.allocGlobalDynamicData(MARKER_estimateSurfaceHeightCache);
            this.allocGlobalDynamicData(MARKER_aquifer);
            this.allocGlobalDynamicData(MARKER_fluidLevelSampler);
            this.allocGlobalDynamicData(MARKER_oreVeinRandom);
            this.allocGlobalDynamicData(MARKER_cacheLike_flatCache);
            this.allocGlobalDynamicData(MARKER_cacheLike_cache2d);
            this.allocGlobalDynamicData(MARKER_cacheLike_interpolator);
        }

        // ---- names and ids --------------------------------------------

        public String nextMethodName() {
            return String.format("method_%d", this.methodIdx++);
        }

        public String nextMethodName(String suffix) {
            return String.format("method_%d_%s", this.methodIdx++, suffix);
        }

        /**
         * The {@code <id>} to emit a function under. Names the prelude already stubbed reuse
         * its {@code <id>}, so existing call sites bind to generated code without patching.
         */
        private int functionIdFor(String name) {
            int existing = this.functionIds.getInt(name);
            if (existing != 0) return existing;
            int fromPrelude = this.prelude.idOfName(name);
            int id = fromPrelude != 0 ? fromPrelude : this.module.nextId();
            this.functionIds.put(name, id);
            if (fromPrelude == 0) {
                this.module.debugName(id, name);
            } else {
                // Replaces a stub, so an existing call site in the prelude reaches it.
                this.preludeBackedFunctions.add(id);
            }
            return id;
        }

        /**
         * Used by {@link SpirVGenFunctionContextImpl} to resolve an OpFunctionCall target.
         */
        public int functionIdByName(String name) {
            return this.functionIds.getInt(name);
        }

        @Override
        public int nextId() {
            return this.module.nextId();
        }

        @Override
        public void debugName(int target, String name) {
            this.module.debugName(target, name);
        }

        // ---- types and constants (delegated, deduplicated) --------------

        @Override
        public int typeVoid() {
            return this.module.typeVoid();
        }

        @Override
        public int typeBool() {
            return this.module.typeBool();
        }

        @Override
        public int typeInt(int width, boolean signed) {
            return this.module.typeInt(width, signed);
        }

        @Override
        public int typeFloat(int width) {
            return this.module.typeFloat(width);
        }

        @Override
        public int typeVector(int componentType, int count) {
            return this.module.typeVector(componentType, count);
        }

        @Override
        public int typeArray(int elementType, int lengthConstantId) {
            return this.module.typeArray(elementType, lengthConstantId);
        }

        @Override
        public int typeRuntimeArray(int elementType) {
            return this.module.typeRuntimeArray(elementType);
        }

        @Override
        public int typeStruct(int... memberTypes) {
            return this.module.typeStruct(memberTypes);
        }

        @Override
        public int typePointer(int storageClass, int pointeeType) {
            return this.module.typePointer(storageClass, pointeeType);
        }

        @Override
        public int typeFunction(int returnType, int... parameterTypes) {
            return this.module.typeFunction(returnType, parameterTypes);
        }

        @Override
        public int typeSampleCtx() {
            int id = this.prelude.idOfName("sample_int32_ctx_t");
            if (id == 0) {
                throw new IllegalStateException("Prelude does not declare sample_int32_ctx_t");
            }
            return id;
        }

        @Override
        public int typeSampleCacheResult() {
            int id = this.prelude.idOfName("cache_result_t");
            if (id == 0) {
                throw new IllegalStateException("Prelude does not declare cache_result_t");
            }
            return id;
        }

        @Override
        public int constBool(boolean value) {
            return this.module.constBool(value);
        }

        @Override
        public int constI32(int value) {
            return this.module.constI32(value);
        }

        @Override
        public int constU32(int value) {
            return this.module.constU32(value);
        }

        @Override
        public int constU64(long value) {
            return this.module.constU64(value);
        }

        @Override
        public int constF32(float value) {
            return this.module.constF32(value);
        }

        @Override
        public int constF64(double value) {
            return this.module.constF64(value);
        }

        // ---- module preamble -------------------------------------------

        @Override
        public void capability(int capability) {
            this.module.capability(capability);
        }

        @Override
        public void extension(String name) {
            this.module.extension(name);
        }

        @Override
        public int extInstImport(String name) {
            return this.module.extInstImport(name);
        }

        @Override
        public void decorate(int target, int decoration, int... operands) {
            this.module.decorate(target, decoration, operands);
        }

        @Override
        public void memberDecorate(int structType, int member, int decoration, int... operands) {
            this.module.memberDecorate(structType, member, decoration, operands);
        }

        /**
         * Builds the type shape glslang produces for
         * {@code layout(buffer_reference, scalar) buffer T { E v[]; }}:
         *
         * <pre>
         * OpDecorate %runtimeArr ArrayStride stride
         * OpDecorate %struct Block
         * OpMemberDecorate %struct 0 Offset 0
         * %struct = OpTypeStruct %runtimeArr
         * %ptr    = OpTypePointer PhysicalStorageBuffer %struct
         * </pre>
         *
         * <p>Deduplicated, because the decorations must not be emitted twice for one type.
         * No {@code OpTypeForwardPointer} is needed -- that is only required when the struct
         * refers back to its own pointer type, which these do not.</p>
         *
         * @param pointeeType element type of the runtime array
         * @param align       element stride in bytes
         * @return the {@code PhysicalStorageBuffer} pointer type
         */
        @Override
        public int bufferReference(int pointeeType, int align) {
            long key = ((long) pointeeType << 32) | (align & 0xFFFFFFFFL);
            int cached = this.bufferReferences.get(key);
            if (cached != 0) return cached;

            int runtimeArray = this.module.typeRuntimeArray(pointeeType);
            this.module.decorate(runtimeArray, SpirV.DECORATION_ARRAY_STRIDE, align);

            int struct = this.module.typeStruct(runtimeArray);
            this.module.decorate(struct, SpirV.DECORATION_BLOCK);
            this.module.memberDecorate(struct, 0, SpirV.DECORATION_OFFSET, 0);

            int pointer = this.module.typePointer(SpirV.STORAGE_CLASS_PHYSICAL_STORAGE_BUFFER, struct);
            this.bufferReferences.put(key, pointer);
            return pointer;
        }

        // ---- functions ---------------------------------------------------

        public void prependConstants(GenerationShapeConfig generationShapeConfig) {
            // The prelude is compiled ahead of time, so these reach it as OpSpecConstants
            // rather than the OpenCL backend's -D defines.
            this.specializationConstants.put("genShapeCfg_minimumY", generationShapeConfig.minimumY());
            this.specializationConstants.put("genShapeCfg_height", generationShapeConfig.height());
            this.specializationConstants.put("genShapeCfg_horizontalSize", generationShapeConfig.horizontalSize());
            this.specializationConstants.put("genShapeCfg_verticalSize", generationShapeConfig.verticalSize());
        }

        public void compileBinding(AstNode node, String id) {
            newDispatcherF64(node, "df_binding_" + id);
        }

        @Override
        public ValuesMethodDef newDispatcher(AstNode node, String id, AstNode.ReturnType returnType) {
            validateNodeType(node, returnType);
            int resultType = returnType == AstNode.ReturnType.F64 ? typeFloat(64) : typeFloat(32);
            int functionType = typeFunction(resultType, typePointer(SpirV.STORAGE_CLASS_FUNCTION, typeSampleCtx()));

            for (SpirVGenFunctionContext.FunctionVariant variant : SpirVGenFunctionContext.FunctionVariant.values()) {
                if (!variant.inDispatcher) continue;
                ValuesMethodDef method = newMethod(node, variant, node.getReturnType());
                String name = id + variant.suffix;
                SpirVGenFunctionContextImpl fn = new SpirVGenFunctionContextImpl(
                        this, null, variant, functionIdFor(name), resultType, functionType);
                fn.returnValue(fn.getDelegateVar(method, node.getReturnType()));
                this.pendingFunctions.add(fn);
            }

            // if      (rw_data != 0 && (flags & ALL)  == ALL)  -> fully cached
            // else if (rw_data != 0 && (flags & FLAT) == FLAT) -> flat cache only
            // else                                            -> uncached
            SpirVGenFunctionContextImpl dispatcher = new SpirVGenFunctionContextImpl(
                    this, null, SpirVGenFunctionContext.FunctionVariant.UNCACHED,
                    functionIdFor(id), resultType, functionType);

            int u32 = typeInt(32, false);
            int u64 = typeInt(64, false);
            int bool = typeBool();

            int hasRwData = dispatcher.op(SpirV.OP_I_NOT_EQUAL, bool, dispatcher.paramRwData(), constU64(0L));

            int allMask = constU32(MASK_ENABLE_ALL_CACHES);
            int allBits = dispatcher.op(SpirV.OP_BITWISE_AND, u32, dispatcher.paramSampleFlags(), allMask);
            int allSet = dispatcher.op(SpirV.OP_I_EQUAL, bool, allBits, allMask);
            int useAll = dispatcher.op(SpirV.OP_LOGICAL_AND, bool, hasRwData, allSet);

            int flatMask = constU32(MASK_ENABLE_FLAT_CACHE);
            int flatBits = dispatcher.op(SpirV.OP_BITWISE_AND, u32, dispatcher.paramSampleFlags(), flatMask);
            int flatSet = dispatcher.op(SpirV.OP_I_EQUAL, bool, flatBits, flatMask);
            int useFlat = dispatcher.op(SpirV.OP_LOGICAL_AND, bool, hasRwData, flatSet);

            int allLabel = dispatcher.newLabel();
            int notAllLabel = dispatcher.newLabel();
            int flatLabel = dispatcher.newLabel();
            int uncachedLabel = dispatcher.newLabel();
            int mergeOuter = dispatcher.newLabel();
            int mergeInner = dispatcher.newLabel();

            dispatcher.selectionMerge(mergeOuter);
            dispatcher.branchConditional(useAll, allLabel, notAllLabel);

            dispatcher.label(allLabel);
            dispatcher.returnValue(callVariant(dispatcher, id, SpirVGenFunctionContext.FunctionVariant.FULLY_CACHED, resultType));

            dispatcher.label(notAllLabel);
            dispatcher.selectionMerge(mergeInner);
            dispatcher.branchConditional(useFlat, flatLabel, uncachedLabel);

            dispatcher.label(flatLabel);
            dispatcher.returnValue(callVariant(dispatcher, id, SpirVGenFunctionContext.FunctionVariant.FLATCACHE_ONLY, resultType));

            dispatcher.label(uncachedLabel);
            dispatcher.returnValue(callVariant(dispatcher, id, SpirVGenFunctionContext.FunctionVariant.UNCACHED, resultType));

            // Both selection constructs need their merge blocks even though every arm returns.
            dispatcher.label(mergeInner);
            dispatcher.opVoid(SpirV.OP_UNREACHABLE);
            dispatcher.label(mergeOuter);
            dispatcher.opVoid(SpirV.OP_UNREACHABLE);

            this.pendingFunctions.add(dispatcher);

            return makeValuesMethodDef(id, node.getReturnType());
        }

        private int callVariant(SpirVGenFunctionContextImpl fn, String id,
                                SpirVGenFunctionContext.FunctionVariant variant, int resultType) {
            return fn.op(SpirV.OP_FUNCTION_CALL, resultType, functionIdFor(id + variant.suffix), fn.getCtxParamId());
        }

        @Override
        public ValuesMethodDefF64 newDispatcherF64(AstNode node) {
            return newDispatcherF64(node, nextMethodName());
        }

        @Override
        public ValuesMethodDefF64 newDispatcherF64(AstNode node, String id) {
            return (ValuesMethodDefF64) newDispatcher(node, id, AstNode.ReturnType.F64);
        }

        @Override
        public ValuesMethodDefF32 newDispatcherF32(AstNode node) {
            return newDispatcherF32(node, nextMethodName());
        }

        @Override
        public ValuesMethodDefF32 newDispatcherF32(AstNode node, String id) {
            return (ValuesMethodDefF32) newDispatcher(node, id, AstNode.ReturnType.F32);
        }

        @Override
        public ValuesMethodDef newMethod(AstNode node, SpirVGenFunctionContext.FunctionVariant variant,
                                         AstNode.ReturnType returnType) {
            validateNodeType(node, returnType);
            if (node instanceof ConstantNodeLike constantNodeLike) {
                return constantNodeLike.getDef();
            }
            String generated = newMethodUnoptimized(node, variant);
            return makeValuesMethodDef(generated, returnType);
        }

        @Override
        public ValuesMethodDefF64 newMethodF64(AstNode node, SpirVGenFunctionContext.FunctionVariant variant) {
            return (ValuesMethodDefF64) newMethod(node, variant, AstNode.ReturnType.F64);
        }

        @Override
        public ValuesMethodDefF32 newMethodF32(AstNode node, SpirVGenFunctionContext.FunctionVariant variant) {
            return (ValuesMethodDefF32) newMethod(node, variant, AstNode.ReturnType.F32);
        }

        public String newMethodUnoptimized(AstNode node, SpirVGenFunctionContext.FunctionVariant variant) {
            return this.methods.computeIfAbsent(new FunctionKey(node, variant), this::newMethod0);
        }

        private String newMethod0(FunctionKey key) {
            String methodName = nextMethodName();
            AstNode.ReturnType returnType = key.node().getReturnType();
            int resultType = returnType == AstNode.ReturnType.F64 ? typeFloat(64) : typeFloat(32);
            int functionType = typeFunction(resultType, typePointer(SpirV.STORAGE_CLASS_FUNCTION, typeSampleCtx()));

            SpirVGenFunctionContextImpl fn = new SpirVGenFunctionContextImpl(
                    this, null, key.variant(), functionIdFor(methodName), resultType, functionType);
            ValuesMethodDef finalVar = fn.newVar(key.node());
            fn.returnValue(fn.getDelegateVar(finalVar, returnType));
            this.pendingFunctions.add(fn);
            return methodName;
        }

        /**
         * @see SpirVPrelude#parameterTypesOf(int)
         */
        public int[] parameterTypesOfFunction(int functionId) {
            return this.prelude.parameterTypesOf(functionId);
        }

        /**
         * @see SpirVPrelude#functionPointerPointee(int)
         */
        public int functionPointerPointee(int typeId) {
            return this.prelude.functionPointerPointee(typeId);
        }

        /**
         * The signature a function must be defined with: the prelude's, when this replaces one of
         * its stubs, because the prelude's own call sites survive and are typed against it.
         */
        private int[] signatureOf(int functionId, int[] fallback) {
            int[] declared = this.prelude.parameterTypesOf(functionId);
            return declared != null ? declared : fallback;
        }

        private int functionTypeOf(int functionId, int returnType, int[] params) {
            int declared = this.prelude.functionTypeOf(functionId);
            return declared != 0 ? declared : typeFunction(returnType, params);
        }

        @Override
        public int preludeFunctionId(String name) {
            int id = this.prelude.idOfName(name);
            if (id == 0) {
                throw new IllegalStateException("Prelude does not define " + name
                        + " (was it eliminated as unreachable?)");
            }
            return id;
        }

        @Override
        public int functionId(ValuesMethodDef target) {
            return this.functionIds.getInt(target.generatedMethod());
        }

        // ---- buffer-resident data (byte offsets, not <id>s) --------------

        @Override
        public int allocGlobalDynamicData(Object data) {
            if (this.globalDynamicDataOffsets.containsKey(data)) {
                return this.globalDynamicDataOffsets.getInt(data);
            }
            int index = this.globalDynamicDataOffsets.size();
            this.globalDynamicDataOffsets.put(data, index);
            return index;
        }

        @Override
        public int allocGlobalConstData(byte[] data, int alignment) {
            if (this.globalConstDataOffsets.containsKey(data)) {
                return this.globalConstDataOffsets.getInt(data);
            }
            // The object has to START aligned, not merely leave an aligned tail behind it:
            // a buffer_reference declares an alignment and reading one through a misaligned
            // address is undefined. OpenCL rounded only the tail and got away with it.
            int startOffset = com.ishland.c2me.base.common.util.MemoryUtil.roundUp(this.globalConstDataTail, alignment);
            this.globalConstDataTail = startOffset + data.length;
            this.globalConstDataOffsets.put(data, startOffset);
            return startOffset;
        }

        @Override
        public int allocGlobalConstDataObject(Object obj) {
            return allocGlobalConstData(bytesObject(obj), 8);
        }

        @Override
        public int getGlobalDynamicDataOffset(Object data) {
            if (!this.globalDynamicDataOffsets.containsKey(data)) {
                throw new IllegalStateException("No global dynamic data offset found");
            }
            return this.globalDynamicDataOffsets.getInt(data);
        }

        @Override
        public int registerFlatCache(CacheLikeNode node) {
            int existing = this.flatCaches.getInt(node);
            if (existing != -1) return existing;
            if (this.cacheFrozen) {
                throw new IllegalStateException("Cannot register more caches");
            }
            int ordinal = this.flatCaches.size();
            this.flatCaches.put(node, ordinal);
            return ordinal;
        }

        @Override
        public int registerCache2d(CacheLikeNode node) {
            int existing = this.cache2ds.getInt(node);
            if (existing != -1) return existing;
            if (this.cacheFrozen) {
                throw new IllegalStateException("Cannot register more caches");
            }
            int ordinal = this.cache2ds.size();
            this.cache2ds.put(node, ordinal);
            return ordinal;
        }

        @Override
        public int registerInterpolator(CacheLikeNode node) {
            int existing = this.interpolators.getInt(node);
            if (existing != -1) return existing;
            if (this.cacheFrozen) {
                throw new IllegalStateException("Cannot register more caches");
            }
            int ordinal = this.interpolators.size();
            this.interpolators.put(node, ordinal);
            return ordinal;
        }

        @SuppressWarnings("unchecked")
        /**
         * Fills the prelude's prefill stubs.
         *
         * <p>The OpenCL backend emitted a kernel per prefill; a Vulkan module has one entry
         * point, so the prelude owns {@code main()} and calls a stub per prefill kind. The flat
         * cache stub switches on the cache index (the OpenCL backend had
         * {@code df_flatcache_prefill_kernel_<i>}); cache2d and interpolator run every index in
         * sequence, as their single OpenCL kernel did.</p>
         */
        /**
         * @param type only this variant's prefill is emitted. The OpenCL backend emitted all
         *             three unconditionally and let the compiler drop the unused ones; here each
         *             prelude variant only keeps the helpers its own kernel needs alive, so
         *             emitting a prefill into the wrong variant would fail to resolve them.
         */
        public void genNoiseKernels(ProgramType type) {
            this.cacheFrozen = true;
            if (type != ProgramType.FLAT_CACHE_PREFILL
                    && type != ProgramType.CACHE2D_PREFILL
                    && type != ProgramType.INTERPOLATOR_PREFILL) {
                return;
            }

            int voidType = typeVoid();
            int u64 = typeInt(64, false);
            int u32 = typeInt(32, false);
            int i32 = typeInt(32, true);
            int f64 = typeFloat(64);
            int ctxPtr = typePointer(SpirV.STORAGE_CLASS_FUNCTION, typeSampleCtx());

            switch (type) {
                case FLAT_CACHE_PREFILL -> genFlatCachePrefill(voidType, u64, u32, i32, f64, ctxPtr);
                case CACHE2D_PREFILL -> genSimplePrefill("df_cache2d_prefill", this.cache2ds,
                        SpirVGenFunctionContext.FunctionVariant.FULLY_CACHED_EXCEPT_CACHE2D,
                        MARKER_cacheLike_cache2d, "df_address_cache2d_buffer",
                        voidType, u64, u32, i32, f64, ctxPtr, false);
                case INTERPOLATOR_PREFILL -> genSimplePrefill("df_interpolator_buffer_prefill", this.interpolators,
                        SpirVGenFunctionContext.FunctionVariant.FLATCACHE_ONLY,
                        MARKER_cacheLike_interpolator, "df_address_interpolator_buffer",
                        voidType, u64, u32, i32, f64, ctxPtr, true);
                default -> throw new IllegalStateException("Unexpected prefill type: " + type);
            }
        }

        /**
         * {@code (const_data, rw_data, extra_out, cacheIndex, offsetX, offsetZ)}
         */
        private void genFlatCachePrefill(int voidType, int u64, int u32, int i32, int f64, int ctxPtr) {
            int functionId = functionIdFor("df_flatcache_prefill");
            int[] params = signatureOf(functionId, new int[]{u64, u64, u64, u32, i32, i32});
            int functionType = functionTypeOf(functionId, voidType, params);
            SpirVGenFunctionContextImpl fn = new SpirVGenFunctionContextImpl(
                    this, null, SpirVGenFunctionContext.FunctionVariant.UNCACHED,
                    functionId, voidType, functionType, params);

            int offset = getGlobalDynamicDataOffset(MARKER_cacheLike_flatCache);
            int data = fn.op(SpirV.OP_FUNCTION_CALL, u64,
                    preludeFunctionId("df_data_offset_global"), fn.getParamId(1), constI32(offset));

            List<CacheLikeNode> nodes = new ArrayList<>(this.flatCaches.keySet());
            if (nodes.isEmpty()) {
                fn.opVoid(SpirV.OP_RETURN);
                this.pendingFunctions.add(fn);
                return;
            }

            int mergeLabel = fn.newLabel();
            int[] caseLabels = new int[nodes.size()];
            int[] literals = new int[nodes.size()];
            for (int i = 0; i < nodes.size(); i++) {
                caseLabels[i] = fn.newLabel();
                literals[i] = i;
            }
            fn.selectionMerge(mergeLabel);
            fn.switchOn(fn.getParamId(3), mergeLabel, literals, caseLabels);

            for (int i = 0; i < nodes.size(); i++) {
                CacheLikeNode node = nodes.get(i);
                validateNodeType(node, AstNode.ReturnType.F64);
                String delegate = newMethodUnoptimized(node.getDelegate(),
                        SpirVGenFunctionContext.FunctionVariant.UNCACHED);

                fn.label(caseLabels[i]);
                // Sampled with a null rw_data: the flat cache is what everything else reads, so
                // it cannot read a cache itself.
                int ctx = buildCtx(fn, ctxPtr, fn.getParamId(0), constU64(0L),
                        biomeToBlock(fn, i32, fn.getParamId(4), "startBiomeX"),
                        constI32(0),
                        biomeToBlock(fn, i32, fn.getParamId(5), "startBiomeZ"),
                        constU32(0));
                int result = fn.op(SpirV.OP_FUNCTION_CALL, f64, functionIdByName(delegate), ctx);
                int index = fn.op(SpirV.OP_FUNCTION_CALL, u32,
                        preludeFunctionId("df_address_flatcache_buffer"), fn.getParamId(1),
                        constU32(i), fn.op(SpirV.OP_BITCAST, u32, fn.getParamId(4)),
                        fn.op(SpirV.OP_BITCAST, u32, fn.getParamId(5)));
                storeDouble(fn, f64, data, index, result);
                storeDoubleIfPresent(fn, f64, u64, fn.getParamId(2), index, result);
                fn.branch(mergeLabel);
            }

            fn.label(mergeLabel);
            fn.opVoid(SpirV.OP_RETURN);
            this.pendingFunctions.add(fn);
        }

        private void genSimplePrefill(String stubName, Object2IntLinkedOpenHashMap<CacheLikeNode> caches,
                                      SpirVGenFunctionContext.FunctionVariant variant, Object marker,
                                      String addressHelper, int voidType, int u64, int u32, int i32,
                                      int f64, int ctxPtr, boolean threeDimensional) {
            int functionId = functionIdFor(stubName);
            int[] params = signatureOf(functionId, threeDimensional
                    ? new int[]{u64, u64, u64, i32, i32, i32}
                    : new int[]{u64, u64, u64, i32, i32});
            int functionType = functionTypeOf(functionId, voidType, params);
            SpirVGenFunctionContextImpl fn = new SpirVGenFunctionContextImpl(
                    this, null, variant, functionId, voidType, functionType, params);

            int offset = getGlobalDynamicDataOffset(marker);
            int data = fn.op(SpirV.OP_FUNCTION_CALL, u64,
                    preludeFunctionId("df_data_offset_global"), fn.getParamId(1), constI32(offset));

            int i = 0;
            for (CacheLikeNode node : caches.keySet()) {
                validateNodeType(node, AstNode.ReturnType.F64);
                String delegate = newMethodUnoptimized(node.getDelegate(), variant);

                int ctx;
                int index;
                if (threeDimensional) {
                    ctx = buildCtx(fn, ctxPtr, fn.getParamId(0), fn.getParamId(1),
                            cellToBlock(fn, i32, fn.getParamId(3), "startCellX", true),
                            cellToBlock(fn, i32, fn.getParamId(4), "startCellY", false),
                            cellToBlock(fn, i32, fn.getParamId(5), "startCellZ", true),
                            constU32(MASK_ENABLE_FLAT_CACHE));
                    index = fn.op(SpirV.OP_FUNCTION_CALL, u32, preludeFunctionId(addressHelper),
                            fn.getParamId(1), constU32(i), fn.getParamId(3), fn.getParamId(4), fn.getParamId(5));
                } else {
                    ctx = buildCtx(fn, ctxPtr, fn.getParamId(0), fn.getParamId(1),
                            addParamsField(fn, i32, fn.getParamId(3), "cache2d_startX"),
                            constI32(0),
                            addParamsField(fn, i32, fn.getParamId(4), "cache2d_startZ"),
                            constU32(MASK_ENABLE_ALL_CACHES));
                    index = fn.op(SpirV.OP_FUNCTION_CALL, u32, preludeFunctionId(addressHelper),
                            fn.getParamId(1), constU32(i),
                            fn.op(SpirV.OP_BITCAST, u32, fn.getParamId(3)),
                            fn.op(SpirV.OP_BITCAST, u32, fn.getParamId(4)));
                }

                int result = fn.op(SpirV.OP_FUNCTION_CALL, f64, functionIdByName(delegate), ctx);
                storeDouble(fn, f64, data, index, result);
                storeDoubleIfPresent(fn, f64, u64, fn.getParamId(2), index, result);
                i++;
            }

            fn.opVoid(SpirV.OP_RETURN);
            this.pendingFunctions.add(fn);
        }

        /**
         * Materialises a sample_int32_ctx_t into a Function-storage local and returns its pointer.
         */
        private int buildCtx(SpirVGenFunctionContextImpl fn, int ctxPtr, int constData, int rwData,
                             int x, int y, int z, int flags) {
            int ctxType = typeSampleCtx();
            int composite = fn.op(SpirV.OP_COMPOSITE_CONSTRUCT, ctxType, constData, rwData, x, y, z, flags);
            int local = fn.newLocal(ctxType);
            fn.store(local, composite);
            return local;
        }

        private int paramsField(SpirVGenFunctionContextImpl fn, int i32, String field) {
            return fn.op(SpirV.OP_FUNCTION_CALL, i32, preludeFunctionId("c2me_params_" + field), fn.getParamId(1));
        }

        private int addParamsField(SpirVGenFunctionContextImpl fn, int i32, int value, String field) {
            return fn.op(SpirV.OP_I_ADD, i32, value, paramsField(fn, i32, field));
        }

        private int biomeToBlock(SpirVGenFunctionContextImpl fn, int i32, int offset, String startField) {
            int biome = addParamsField(fn, i32, offset, startField);
            return fn.op(SpirV.OP_FUNCTION_CALL, i32, preludeFunctionId("math_biome2block"), biome);
        }

        private int cellToBlock(SpirVGenFunctionContextImpl fn, int i32, int rel, String startField,
                                boolean horizontal) {
            int cell = addParamsField(fn, i32, rel, startField);
            int blockCount = fn.op(SpirV.OP_FUNCTION_CALL, typeInt(32, false),
                    preludeFunctionId(horizontal ? "genShapeCfg_horizontalCellBlockCount"
                            : "genShapeCfg_verticalCellBlockCount"));
            return fn.op(SpirV.OP_I_MUL, i32, cell, fn.op(SpirV.OP_BITCAST, i32, blockCount));
        }

        private void storeDouble(SpirVGenFunctionContextImpl fn, int f64, int base, int index, int value) {
            int blockPointer = bufferReference(f64, Double.BYTES);
            int elementPointer = typePointer(SpirV.STORAGE_CLASS_PHYSICAL_STORAGE_BUFFER, f64);
            int pointer = fn.op(SpirV.OP_CONVERT_U_TO_PTR, blockPointer, base);
            int element = fn.op(SpirV.OP_ACCESS_CHAIN, elementPointer, pointer, constI32(0), index);
            fn.opVoid(SpirV.OP_STORE, element, value, SpirV.MEMORY_ACCESS_ALIGNED, Double.BYTES);
        }

        /**
         * extra_out is optional; the OpenCL backend guarded it the same way.
         */
        private void storeDoubleIfPresent(SpirVGenFunctionContextImpl fn, int f64, int u64, int base,
                                          int index, int value) {
            int present = fn.op(SpirV.OP_I_NOT_EQUAL, typeBool(), base, constU64(0L));
            com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.SpirVEmitterUtil.ifElse(fn, present,
                    () -> storeDouble(fn, f64, base, index, value),
                    () -> {
                    });
        }

        public void genBiomeTree(BiomeSource biomeSource) {
            if (biomeSource instanceof MultiNoiseBiomeSource multiNoiseBiomeSource) {
                MultiNoiseUtil.Entries<RegistryEntry<Biome>> entries =
                        ((IMultiNoiseBiomeSource) multiNoiseBiomeSource).invokeGetBiomeEntries();
                if (entries != null) {
                    MultiNoiseUtil.SearchTree<RegistryEntry<Biome>> tree =
                            ((IMultiNoiseUtilEntries<RegistryEntry<Biome>>) entries).getTree();
                    int globalOffset;
                    int nodeCount;
                    int treeDepth;
                    try (Arena arena = Arena.ofConfined()) {
                        BindingsTemplate.NativeBiomeSearchTree nativeBiomeSearchTree =
                                BindingsTemplate.biome_search_tree_node$create(arena, tree);
                        byte[] bytes = new byte[Math.toIntExact(nativeBiomeSearchTree.segment().byteSize())];
                        MemorySegment.copy(nativeBiomeSearchTree.segment(), ValueLayout.JAVA_BYTE, 0, bytes, 0, bytes.length);
                        globalOffset = this.allocGlobalConstData(bytes, 8);
                        nodeCount = nativeBiomeSearchTree.node_c();
                        treeDepth = nativeBiomeSearchTree.tree_depth();
                        this.biomeMappings = nativeBiomeSearchTree.biomes();
                    }
                    // The OpenCL backend emitted these as `constant const uint32_t` in the
                    // generated source. Here the prelude is precompiled, so they arrive as
                    // specialization constants instead.
                    this.specializationConstants.put("biome_multinoise_tree_offset", globalOffset);
                    this.specializationConstants.put("biome_multinoise_tree_nodes_c", nodeCount);
                    this.specializationConstants.put("BIOME_SEARCH_TREE_MAX_DEPTH", treeDepth);
                    return;
                }
            }
            this.specializationConstants.put("biome_multinoise_tree_offset", 0);
            this.specializationConstants.put("biome_multinoise_tree_nodes_c", 0);
            this.specializationConstants.put("BIOME_SEARCH_TREE_MAX_DEPTH", 1);
        }

        /**
         * Mirrors OpenCLCGen.buildConstData.
         */
        private byte[] buildConstData() {
            byte[] constData = new byte[this.globalConstDataTail];
            for (Object2IntMap.Entry<byte[]> entry : this.globalConstDataOffsets.object2IntEntrySet()) {
                byte[] data = entry.getKey();
                int offset = entry.getIntValue();
                if (offset + data.length > constData.length) {
                    throw new IllegalStateException("Const data offset out of bounds: "
                            + offset + " + " + data.length + " > " + constData.length);
                }
                System.arraycopy(data, 0, constData, offset, data.length);
            }
            return constData;
        }

        /**
         * Resolves each named constant to the SpecId the prelude declared for it.
         */
        private Int2IntOpenHashMap resolveSpecializationConstants() {
            Int2IntOpenHashMap resolved = new Int2IntOpenHashMap();
            for (Object2IntMap.Entry<String> entry : this.specializationConstants.object2IntEntrySet()) {
                int specId = this.prelude.specIdOfName(entry.getKey());
                if (specId < 0) {
                    throw new IllegalStateException("Prelude declares no specialization constant named "
                            + entry.getKey());
                }
                resolved.put(specId, entry.getIntValue());
            }
            return resolved;
        }

        // ---- output -------------------------------------------------------

        /**
         * Flushes the generated functions and serialises this variant's module.
         *
         * <p>Bindings are compiled for every variant, because that traversal is what registers
         * the cache nodes and fixes their ordinals -- those must agree across variants, since the
         * ordinal is baked into generated code while the buffer is sized once. But a variant's
         * prelude only retains the stubs its own kernel calls, so most of that output is
         * unreachable. Anything not transitively called from a replaced stub is dropped rather
         * than serialised and left for the driver to strip.</p>
         */
        public int[] buildModule() {
            // Prelude bodies first, minus the stubs we redefine below under the same <id>.
            this.prelude.seedFunctions(this.module, this.preludeBackedFunctions);

            java.util.Map<Integer, SpirVGenFunctionContextImpl> byId = new java.util.HashMap<>();
            for (SpirVGenFunctionContextImpl fn : this.pendingFunctions) {
                byId.put(fn.getFunctionId(), fn);
            }

            it.unimi.dsi.fastutil.ints.IntOpenHashSet reachable = new it.unimi.dsi.fastutil.ints.IntOpenHashSet();
            java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
            for (int root : this.preludeBackedFunctions) {
                if (byId.containsKey(root) && reachable.add(root)) queue.add(root);
            }
            while (!queue.isEmpty()) {
                SpirVGenFunctionContextImpl fn = byId.get(queue.poll());
                if (fn == null) continue;
                for (int callee : fn.getCallees()) {
                    if (byId.containsKey(callee) && reachable.add(callee)) queue.add(callee);
                }
            }

            int dropped = 0;
            for (SpirVGenFunctionContextImpl fn : this.pendingFunctions) {
                if (reachable.contains(fn.getFunctionId())) {
                    fn.finish(this.module);
                } else {
                    dropped++;
                }
            }
            if (dropped != 0) {
                LOGGER.debug("Dropped {} unreachable generated functions of {}",
                        dropped, this.pendingFunctions.size());
            }
            this.pendingFunctions.clear();
            return this.module.toWords();
        }

        public GeneratedSpirVSource buildSource(EnumMap<ProgramType, int[]> modules) {
            return new GeneratedSpirVSource(
                    ordinal.getAndIncrement(),
                    modules,
                    buildConstData(),
                    this.globalDynamicDataOffsets,
                    this.flatCaches.size(),
                    this.cache2ds.size(),
                    this.interpolators.size(),
                    resolveSpecializationConstants(),
                    this.biomeMappings,
                    null
            );
        }

    }

}
