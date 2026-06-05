/*
 * All Rights Reserved
 *
 * Copyright (c) 2025-2026 ishland
 *
 * All rights reserved. Do not redistribute.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.accel.opencl.common.compiler;

import com.ishland.c2me.base.common.util.MemoryUtil;
import com.ishland.c2me.base.mixin.access.IDoublePerlinNoiseSampler;
import com.ishland.c2me.base.mixin.access.IMultiNoiseBiomeSource;
import com.ishland.c2me.base.mixin.access.IMultiNoiseUtilEntries;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.opto.OptoPasses;
import com.ishland.c2me.opts.dfc.common.gen.GenDumper;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.ishland.c2me.opts.natives_math.common.BindingsTemplate;
import com.ishland.flowsched.util.Assertions;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Spline;
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

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class OpenCLCGen {

    public static final Object MARKER_localOffsetTable = new Object(); // unused
//    public static final Object MARKER_structureWeightSamplerTable = new Object();
    public static final Object MARKER_estimateSurfaceHeightCache = new Object();
    public static final Object MARKER_aquifer = new Object();
    public static final Object MARKER_fluidLevelSampler = new Object();
    public static final Object MARKER_oreVeinRandom = new Object();
    public static final Object MARKER_cacheLike_interpolator = new Object();
    public static final Object MARKER_cacheLike_flatCache = new Object();
    public static final Object MARKER_cacheLike_cache2d = new Object();

    private static final AtomicLong ordinal = new AtomicLong();

    // df_binding_def(barrier);
    // df_binding_def(fluid_level_floodedness);
    // df_binding_def(fluid_level_spread);
    // df_binding_def(lava);
    // df_binding_def(temperature);
    // df_binding_def(vegetation);
    // df_binding_def(continents);
    // df_binding_def(erosion);
    // df_binding_def(depth);
    // df_binding_def(ridges);
    // df_binding_def(initial_density_without_jaggedness);
    // df_binding_def(final_density);
    // df_binding_def(vein_toggle);
    // df_binding_def(vein_ridged);
    // df_binding_def(vein_gap);

    public static GeneratedCLSource compile(NoiseRouter noiseRouter, GenerationShapeConfig generationShapeConfig, Reference2ReferenceMap<DensityFunction, OptoPasses.AstPair> optoCache, DensityFunction finalFinalDensity, BiomeSource biomeSource) {
        ContextImpl context = new ContextImpl();
        context.prependConstants(generationShapeConfig);

        Object2ReferenceLinkedOpenHashMap<String, OptoPasses.AstPair> dfs = new Object2ReferenceLinkedOpenHashMap<>();
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

        context.genNoiseKernels();

        context.genBiomeTree(biomeSource);

        GeneratedCLSource original = context.build();
        String name = "DfcCompiled_" + original.getOrdinal();
        Path path = GenDumper.dumpCL(name, original.getGeneratedSource().getBytes(StandardCharsets.UTF_8));
        GenDumper.dumpDot(name, path, dfs);
        return new GeneratedCLSource(
                original.getOrdinal(),
                original.getGeneratedSource(),
                original.getConstData(),
                original.getGlobalDynamicDataOffsets(),
                original.getFlatCachePrefills(),
                original.getCache2dPrefills(),
                original.getInterpolatorPrefills(),
                original.getDefines(),
                original.getBiomeMappings(),
                path
        );
    }

    private static OptoPasses.AstPair optimizeCached(DensityFunction densityFunction, Reference2ReferenceMap<DensityFunction, OptoPasses.AstPair> optoCache) {
        return optoCache.computeIfAbsent(densityFunction, (DensityFunction df) -> OptoPasses.optimizeOCL(McToAst.toAst(df)));
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

    public static class ContextImpl implements OpenCLCGenContext {

        private final StringBuilder pendingSource = new StringBuilder();
        private final Object2ReferenceOpenHashMap<AstNode, String> methods = new Object2ReferenceOpenHashMap<>();
        private final Object2ReferenceOpenHashMap<Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper>, String> splineMethods = new Object2ReferenceOpenHashMap<>();
        private final Object2ReferenceOpenHashMap<Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper>, String> splineMethodsCache1 = new Object2ReferenceOpenHashMap<>();
        private final Reference2IntLinkedOpenHashMap<Object> globalDynamicDataOffsets = new Reference2IntLinkedOpenHashMap<>();
        private final ArrayList<CacheLikeNode> flatCaches = new ArrayList<>();
        private final ArrayList<CacheLikeNode> interpolators = new ArrayList<>();
        private final ArrayList<CacheLikeNode> cache2ds = new ArrayList<>();
        private final Object2ReferenceOpenHashMap<String, String> defines = new Object2ReferenceOpenHashMap<>();
        private final Random rng = new Random(1234);
        private RegistryEntry<Biome>[] biomeMappings = null;

        private final int localOffsetTableOffset = this.allocGlobalDynamicData(MARKER_localOffsetTable); // should always be zero

        private int globalConstDataTail = 16;
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

        {
            this.globalDynamicDataOffsets.defaultReturnValue(Integer.MAX_VALUE);
            this.allocGlobalDynamicData(DensityFunctionTypes.Beardifier.INSTANCE);
//            this.allocGlobalDynamicData(MARKER_structureWeightSamplerTable);
            this.allocGlobalDynamicData(MARKER_estimateSurfaceHeightCache);
            this.allocGlobalDynamicData(MARKER_aquifer);
            this.allocGlobalDynamicData(MARKER_fluidLevelSampler);
            this.allocGlobalDynamicData(MARKER_oreVeinRandom);
            this.allocGlobalDynamicData(MARKER_cacheLike_flatCache);
            this.allocGlobalDynamicData(MARKER_cacheLike_cache2d);
            this.allocGlobalDynamicData(MARKER_cacheLike_interpolator);
        }

        private int methodIdx = 0;

        @Override
        public String nextMethodName() {
            return String.format("method_%d", methodIdx++);
        }

        @Override
        public String nextMethodName(String suffix) {
            return String.format("method_%d_%s", methodIdx++, suffix);
        }

        public void prependConstants(GenerationShapeConfig generationShapeConfig) {
            // extern constant const int32_t genShapeCfg_minimumY;
            // extern constant const int32_t genShapeCfg_height;
            // extern constant const uint32_t genShapeCfg_horizontalSize;
            // extern constant const uint32_t genShapeCfg_verticalSize;
            this.pendingSource
                    .append("constant const int32_t genShapeCfg_minimumY = ").append(generationShapeConfig.minimumY()).append(";\n")
                    .append("constant const int32_t genShapeCfg_height = ").append(generationShapeConfig.height()).append(";\n")
                    .append("constant const uint32_t genShapeCfg_horizontalSize = ").append(generationShapeConfig.horizontalSize()).append(";\n")
                    .append("constant const uint32_t genShapeCfg_verticalSize = ").append(generationShapeConfig.verticalSize()).append(";\n");
        }

        public void compileBinding(AstNode node, String id) {
            ValuesMethodDefD method = this.newMethod(node);
            this.pendingSource
                    .append("static __attribute__((pure)) double df_binding_").append(id).append(signature).append(" {\n")
                    .append("    ").append("return ").append(this.callDelegate(method)).append(";\n")
                    .append("}\n");
        }

        @Override
        public ValuesMethodDefD newMethod(AstNode node) {
            if (node instanceof ConstantNode constantNode) {
                return new ValuesMethodDefD(constantNode.getValue());
            } else {
                String generated = this.newMethodUnoptimized(node);
                return new ValuesMethodDefD(generated);
            }
        }

        public String newMethodUnoptimized(AstNode node) {
            return this.methods.computeIfAbsent(node, (AstNode node1) -> this.newMethod(() -> OpenCLCGenRegistry.doCLGen(node1, this), nextMethodName(node.getClass().getSimpleName())));
        }

        private String newMethod(Supplier<String> generator, String name) {
            String functionBody = generator.get();
            this.pendingSource
                    .append("static __attribute__((pure)) double ").append(name).append(signature).append(" {\n")
                    .append(getFillerOrNot().indent(4))
                    .append(functionBody.indent(4))
                    .append("}\n");
            return name;
        }

        @Override
        public String getFillerOrNot() {
            return this.rng.nextInt(16) == 0 ? filler : "";
        }

        public void callDelegate(StringBuilder b, ValuesMethodDefD target) {
            if (target.isConst()) {
                b.append(OpenCLCGen.literal(target.constValue()));
            } else {
                b.append(target.generatedMethod()).append("(ctx)");
            }
        }

        @Override
        public String callDelegate(ValuesMethodDefD target) {
            StringBuilder b = new StringBuilder();
            callDelegate(b, target);
            return b.toString();
        }

        @Override
        public int allocGlobalDynamicData(Object data) {
            if (this.globalDynamicDataOffsets.containsKey(data)) {
                return this.globalDynamicDataOffsets.getInt(data);
            }
            int ordinal = this.globalDynamicDataOffsets.size();
            this.globalDynamicDataOffsets.put(data, ordinal);
            return ordinal;
        }

        @Override
        public int allocGlobalConstData(byte[] data, int alignment) {
            if (this.globalConstDataOffsets.containsKey(data)) {
                return this.globalConstDataOffsets.getInt(data);
            }
            int startOffset = this.globalConstDataTail;
            this.globalConstDataTail = MemoryUtil.roundUp(this.globalConstDataTail, alignment) + data.length;
            this.globalConstDataOffsets.put(data, startOffset);
            return startOffset;
        }

        @Override
        public int allocGlobalConstDataObject(Object obj) {
            byte[] bytes = OpenCLCGen.bytesObject(obj);
            return this.allocGlobalConstData(bytes, 8);
        }

        @Override
        public int getGlobalDynamicDataOffset(Object data) {
            if (!this.globalDynamicDataOffsets.containsKey(data)) {
                throw new IllegalStateException("No global dynamic data offset found");
            }
            return this.globalDynamicDataOffsets.getInt(data);
        }

        @Override
        public String getCachedSplineMethod(Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline, boolean cache1) {
            return (cache1 ? this.splineMethodsCache1 : this.splineMethods).get(spline);
        }

        @Override
        public void cacheSplineMethod(Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline, String method, boolean cache1) {
            (cache1 ? this.splineMethodsCache1 : this.splineMethods).put(spline, method);
        }

        @Override
        public int registerFlatCache(CacheLikeNode node) {
            if (!((Object) node.getCacheLike() instanceof DensityFunctionTypes.Wrapping wrapping)) {
                throw new UnsupportedOperationException("Can only gen wrapping");
            }
            if (wrapping.type() != DensityFunctionTypes.Wrapping.Type.FLAT_CACHE) {
                throw new UnsupportedOperationException("Can only gen flat cache");
            }
            int index = this.flatCaches.size();
            this.flatCaches.add(node);
            return index;
        }

        @Override
        public int registerCache2d(CacheLikeNode node) {
            if (!((Object) node.getCacheLike() instanceof DensityFunctionTypes.Wrapping wrapping)) {
                throw new UnsupportedOperationException("Can only gen wrapping");
            }
            if (wrapping.type() != DensityFunctionTypes.Wrapping.Type.CACHE2D) {
                throw new UnsupportedOperationException("Can only gen cache2d");
            }
            int index = this.cache2ds.size();
            this.cache2ds.add(node);
            return index;
        }

        @Override
        public int registerInterpolator(CacheLikeNode node) {
            if (!((Object) node.getCacheLike() instanceof DensityFunctionTypes.Wrapping wrapping)) {
                throw new UnsupportedOperationException("Can only gen wrapping");
            }
            if (wrapping.type() != DensityFunctionTypes.Wrapping.Type.INTERPOLATED) {
                throw new UnsupportedOperationException("Can only gen interpolator");
            }
            int index = this.interpolators.size();
            this.interpolators.add(node);
            return index;
        }

        public void genNoiseKernels() {
            final ArrayList<String> flatCachePrefills = new ArrayList<>();
            final ArrayList<String> interpolatorPrefills = new ArrayList<>();
            final ArrayList<String> cache2dPrefills = new ArrayList<>();

            {
                ArrayList<CacheLikeNode> caches = this.flatCaches;
                int offset = this.getGlobalDynamicDataOffset(MARKER_cacheLike_flatCache);
                for (int i = 0, cachesSize = caches.size(); i < cachesSize; i++) {
                    CacheLikeNode node = caches.get(i);
                    String nodeName = this.methods.get(node);
                    Assertions.assertTrue(nodeName != null);
                    String delegateName = this.methods.get(node.getDelegate());
                    Assertions.assertTrue(delegateName != null);
                    String name = "df_flatcache_prefill_" + nodeName;

                    // this kernel assumes it is launched with the appropriate global work size
                    // workgroup sizes doesn't matter
                    this.pendingSource
                            .append("static void ").append(name).append("(global const void * restrict const const_data, global void * restrict const rw_data, global double * restrict const extra_out) {\n")
                            .append("    ").append("global const worldgen_params_t * restrict params = rw_data;\n")
                            .append("    ").append("global double * restrict data = df_data_offset_global(rw_data, ").append(offset).append(");\n")
                            .append("    ").append("int32_t offsetX = get_global_id(0);\n")
                            .append("    ").append("int32_t offsetZ = get_global_id(1);\n")
                            .append("    ").append("uint32_t index = df_address_flatcache_buffer(params, ").append(i).append(", offsetX, offsetZ);\n")
                            .append("    ").append("const double result = ").append(delegateName).append("(make_sample_int32_ctx(const_data, NULL, math_biome2block(offsetX + params->startBiomeX), 0, math_biome2block(offsetZ + params->startBiomeZ), 0));\n")
                            .append("    ").append("data[index] = result;\n")
                            .append("    ").append("if (extra_out) extra_out[index] = result;\n")
                            .append("}\n");

                    flatCachePrefills.add(name);
                }
            }

            {
                ArrayList<CacheLikeNode> caches = this.cache2ds;
                int offset = this.getGlobalDynamicDataOffset(MARKER_cacheLike_cache2d);
                for (int i = 0, cachesSize = caches.size(); i < cachesSize; i++) {
                    CacheLikeNode node = caches.get(i);
                    String nodeName = this.methods.get(node);
                    Assertions.assertTrue(nodeName != null);
                    String delegateName = this.methods.get(node.getDelegate());
                    Assertions.assertTrue(delegateName != null);
                    String name = "df_cache2d_prefill_" + nodeName;

                    // this kernel assumes it is launched with the appropriate global work size
                    // workgroup sizes doesn't matter
                    this.pendingSource
                            .append("static FUNC_NOINLINE void ").append(name).append("(global const void * restrict const const_data, global void * restrict const rw_data, global double * restrict const extra_out) {\n")
                            .append("    ").append("global const worldgen_params_t * restrict params = rw_data;\n")
                            .append("    ").append("global double * restrict data = df_data_offset_global(rw_data, ").append(offset).append(");\n")
                            .append("    ").append("int32_t offsetX = get_global_id(0);\n")
                            .append("    ").append("int32_t offsetZ = get_global_id(1);\n")
                            .append("    ").append("uint32_t index = df_address_cache2d_buffer(params, ").append(i).append(", offsetX, offsetZ);\n")
                            .append("    ").append("const double result = ").append(delegateName).append("(make_sample_int32_ctx(const_data, rw_data, offsetX + params->cache2d_startX, 0, offsetZ + params->cache2d_startZ, MASK_isInterpolation | MASK_inInterpolationLoop));\n")
                            .append("    ").append("data[index] = result;\n")
                            .append("    ").append("if (extra_out) extra_out[index] = result;\n")
                            .append("}\n");

                    cache2dPrefills.add(name);
                }
            }

            {
                ArrayList<CacheLikeNode> cacheLikeNodes = this.interpolators;
                int offset = this.getGlobalDynamicDataOffset(MARKER_cacheLike_interpolator);
                for (int i = 0, cacheLikeNodesSize = cacheLikeNodes.size(); i < cacheLikeNodesSize; i++) {
                    CacheLikeNode node = cacheLikeNodes.get(i);
                    String nodeName = this.methods.get(node);
                    Assertions.assertTrue(nodeName != null);
                    String delegateName = this.methods.get(node.getDelegate());
                    Assertions.assertTrue(delegateName != null);

                    {
                        String name = "df_interpolator_buffer_prefill_" + nodeName;

                        // vanilla interpolation area:
                        // cellX: [startCellX, startCellX + horizontalCellCount + 1)
                        // cellY: [startCellY, startCellY + verticalCellCount + 1)
                        // cellZ: [startCellZ, startCellZ + horizontalCellCount + 1)
                        // with (0, 0, 0) pos in each cell

                        // this kernel is supposed to be launched with global work size of (horizontalCellCount + 1, verticalCellCount + 1, horizontalCellCount + 1)
                        // workgroup size doesn't matter
                        this.pendingSource
                                .append("static FUNC_NOINLINE void ").append(name).append("(global const void * restrict const const_data, global void * restrict const rw_data, global double * restrict const extra_out) {\n")
                                .append("    ").append("global const worldgen_params_t * restrict params = rw_data;\n")
                                .append("    ").append("global double *data = df_data_offset_global(rw_data, ").append(offset).append(");\n")
                                .append("    ").append("int32_t cellRelX = get_global_id(0);\n")
                                .append("    ").append("int32_t cellRelY = get_global_id(2);\n")
                                .append("    ").append("int32_t cellRelZ = get_global_id(1);\n")
                                .append("    ").append("int32_t cellX = cellRelX + params->startCellX;\n")
                                .append("    ").append("int32_t cellY = cellRelY + params->startCellY;\n")
                                .append("    ").append("int32_t cellZ = cellRelZ + params->startCellZ;\n")
                                .append("    ").append("uint32_t index = df_address_interpolator_buffer(params, ").append(i).append(", cellRelX, cellRelY, cellRelZ);\n")
                                .append("    ").append("const double result = ").append(delegateName).append("(make_sample_int32_ctx(const_data, rw_data, cellX * genShapeCfg_horizontalCellBlockCount(), cellY * genShapeCfg_verticalCellBlockCount(), cellZ * genShapeCfg_horizontalCellBlockCount(), 0));\n")
                                .append("    ").append("data[index] = result;\n")
                                .append("    ").append("if (extra_out) extra_out[index] = result;\n")
                                .append("}\n");

                        interpolatorPrefills.add(name);
                    }
                }

            }

            if (!flatCachePrefills.isEmpty()) {
                this.pendingSource
                        .append("#ifdef DF_COMPILE_").append(ProgramType.FLAT_CACHE_PREFILL).append("\n");
                for (int i = 0, flatCachePrefillsSize = flatCachePrefills.size(); i < flatCachePrefillsSize; i++) {
                    String name = flatCachePrefills.get(i);
                    this.pendingSource.append("kernel __attribute__((reqd_work_group_size(16, 16, 1))) void df_flatcache_prefill_kernel_").append(i).append("(global const void * restrict const const_data, global void * restrict const rw_data, global double * restrict const extra_out) {\n")
                            .append("    ").append("if (!const_data || !rw_data || !extra_out) {\n")
                            .append("    ").append("    ").append("#ifdef DEBUG\n")
                            .append("    ").append("    ").append("printf(\"trap: !const_data || !rw_data || !extra_out\\n const_data=%p rw_data=%p extra_out=%p\\n\", const_data, rw_data, extra_out);\n")
                            .append("    ").append("    ").append("#endif\n")
                            .append("    ").append("    ").append("__builtin_trap();\n")
                            .append("    ").append("    ").append("__builtin_unreachable();\n")
                            .append("    ").append("    ").append("return;\n")
                            .append("    ").append("}\n")
                            .append("    ").append(name).append("(const_data, rw_data, extra_out);\n")
                            .append("}\n");
//                    this.pendingSource
//                            .append("    ").append(name).append("(const_data, rw_data, extra_out);\n");
                }
                this.pendingSource
                        .append("#endif\n");
            }

            if (!cache2dPrefills.isEmpty()) {
                this.pendingSource
                        .append("#ifdef DF_COMPILE_").append(ProgramType.CACHE2D_PREFILL).append("\n")
                        .append("kernel __attribute__((reqd_work_group_size(8, 8, 1))) void df_cache2d_prefill_kernel(global const void * restrict const const_data, global void * restrict const rw_data) {\n")
                        .append("    ").append("if (!const_data || !rw_data) {\n")
                        .append("    ").append("    ").append("#ifdef DEBUG\n")
                        .append("    ").append("    ").append("printf(\"trap: !const_data || !rw_data\\n const_data=%p rw_data=%p\\n\", const_data, rw_data);\n")
                        .append("    ").append("    ").append("#endif\n")
                        .append("    ").append("    ").append("__builtin_trap();\n")
                        .append("    ").append("    ").append("__builtin_unreachable();\n")
                        .append("    ").append("    ").append("return;\n")
                        .append("    ").append("}\n")
                        .append("\n");
                for (int i = 0, cache2dPrefillsSize = cache2dPrefills.size(); i < cache2dPrefillsSize; i++) {
                    String name = cache2dPrefills.get(i);
                    this.pendingSource
                            .append("    ").append("if (get_global_id(2) == ").append(i).append(") {\n")
                            .append("    ").append("    ").append(name).append("(const_data, rw_data, NULL);\n")
                            .append("    ").append("    ").append("return;\n")
                            .append("    ").append("}\n");
//                    this.pendingSource
//                            .append("    ").append(name).append("(const_data, rw_data, extra_out);\n");
                }
                this.pendingSource
                        .append("}\n")
                        .append("#endif\n");
            }

            if (!interpolatorPrefills.isEmpty()) {
                this.pendingSource
                        .append("#ifdef DF_COMPILE_").append(ProgramType.INTERPOLATOR_PREFILL).append("\n")
                        .append("kernel void df_interpolator_buffer_prefill_kernel(global const void * restrict const const_data, global void * restrict const rw_data) {\n")
                        .append("    ").append("if (!const_data || !rw_data) {\n")
                        .append("    ").append("    ").append("#ifdef DEBUG\n")
                        .append("    ").append("    ").append("printf(\"trap: !const_data || !rw_data\\n const_data=%p rw_data=%p\\n\", const_data, rw_data);\n")
                        .append("    ").append("    ").append("#endif\n")
                        .append("    ").append("    ").append("__builtin_trap();\n")
                        .append("    ").append("    ").append("__builtin_unreachable();\n")
                        .append("    ").append("    ").append("return;\n")
                        .append("    ").append("}\n")
                        .append("\n");
                for (String name : interpolatorPrefills) {
                    this.pendingSource
                            .append("    ").append(name).append("(const_data, rw_data, NULL);\n");
                }
                this.pendingSource
                        .append("}\n")
                        .append("#endif\n");
            }
        }

        public void genBiomeTree(BiomeSource biomeSource) {
            if (biomeSource instanceof MultiNoiseBiomeSource multiNoiseBiomeSource) {
                MultiNoiseUtil.Entries<RegistryEntry<Biome>> entries = ((IMultiNoiseBiomeSource) multiNoiseBiomeSource).invokeGetBiomeEntries();
                if (entries != null) {
                    MultiNoiseUtil.SearchTree<RegistryEntry<Biome>> tree = ((IMultiNoiseUtilEntries<RegistryEntry<Biome>>) entries).getTree();
                    int globalOffset;
                    int nodeCount;
                    int treeDepth;
                    try (Arena arena = Arena.ofConfined()) {
                        BindingsTemplate.NativeBiomeSearchTree nativeBiomeSearchTree = BindingsTemplate.biome_search_tree_node$create(arena, tree);
                        byte[] bytes = new byte[Math.toIntExact(nativeBiomeSearchTree.segment().byteSize())];
                        MemorySegment.copy(nativeBiomeSearchTree.segment(), ValueLayout.JAVA_BYTE, 0, bytes, 0, bytes.length);
                        globalOffset = this.allocGlobalConstData(bytes, 8);
                        nodeCount = nativeBiomeSearchTree.node_c();
                        treeDepth = nativeBiomeSearchTree.tree_depth();
                        this.biomeMappings = nativeBiomeSearchTree.biomes();
                    }
                    this.pendingSource
                            .append("constant const uint32_t biome_multinoise_tree_offset = ").append(globalOffset).append(";\n")
                            .append("constant const uint32_t biome_multinoise_tree_nodes_c = ").append(nodeCount).append(";\n");
                    this.defines.put("BIOME_SEARCH_TREE_MAX_DEPTH", String.valueOf(treeDepth));
                    return;
                }
            }
            this.pendingSource
                    .append("constant const uint32_t biome_multinoise_tree_offset = 0;\n")
                    .append("constant const uint32_t biome_multinoise_tree_nodes_c = 0;\n");
            this.defines.put("BIOME_SEARCH_TREE_MAX_DEPTH", "1");
        }

        @Override
        public void appendRaw(String raw) {
            this.pendingSource.append(raw);
        }

        private byte[] buildConstData() {
            byte[] constData = new byte[this.globalConstDataTail];
            for (Object2IntMap.Entry<byte[]> entry : this.globalConstDataOffsets.object2IntEntrySet()) {
                byte[] data = entry.getKey();
                int offset = entry.getIntValue();
                if (offset + data.length > constData.length) {
                    throw new IllegalStateException("Const data offset out of bounds: " + offset + " + " + data.length + " > " + constData.length);
                }
                System.arraycopy(data, 0, constData, offset, data.length);
            }
            return constData;
        }

        public GeneratedCLSource build() {
            Assertions.assertTrue(this.localOffsetTableOffset == 0);
            return new GeneratedCLSource(
                    ordinal.incrementAndGet(),
                    this.pendingSource.toString(),
                    this.buildConstData(),
                    this.globalDynamicDataOffsets,
                    this.flatCaches.size(),
                    this.cache2ds.size(),
                    this.interpolators.size(),
                    this.defines,
                    this.biomeMappings,
                    null
            );
        }

    }

    public static String literal(double d) {
        return Double.toHexString(d);
    }

    public static String literal(float d) {
        return Float.toHexString(d) + "f";
    }

    public static String literal(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        HexFormat.of().withPrefix("0x").withDelimiter(", ").formatHex(builder, bytes);
        builder.append(" }");
        return builder.toString();
    }

    public static String literal(int[] ints) {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        for (int num : ints) {
            builder.append(num).append(", ");
        }
        builder.append(" }");
        return builder.toString();
    }

    public static String literal(float[] floats) {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        for (float f : floats) {
            builder.append(Float.toHexString(f)).append("f, ");
        }
        builder.append("}");
        return builder.toString();
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

}
