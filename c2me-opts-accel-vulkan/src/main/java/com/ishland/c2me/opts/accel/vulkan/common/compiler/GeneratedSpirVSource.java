package com.ishland.c2me.opts.accel.vulkan.common.compiler;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;

import java.nio.file.Path;
import java.util.EnumMap;

public class GeneratedSpirVSource {

    private final long ordinal;
    private final EnumMap<SpirVGen.ProgramType, int[]> modules;
    private final byte[] constData;
    private final Reference2IntLinkedOpenHashMap<Object> globalDynamicDataOffsets;
    private final int flatCachePrefills;
    private final int cache2dPrefills;
    private final int interpolatorPrefills;
    private final Int2IntOpenHashMap specializationConstants;
    private final RegistryEntry<Biome>[] biomeMappings;
    private final Path dumpedPath;

    public GeneratedSpirVSource(long ordinal, EnumMap<SpirVGen.ProgramType, int[]> modules, byte[] constData, Reference2IntLinkedOpenHashMap<Object> globalDynamicDataOffsets, int flatCachePrefills, int cache2dPrefills, int interpolatorPrefills, Int2IntOpenHashMap specializationConstants, RegistryEntry<Biome>[] biomeMappings, Path dumpedPath) {
        this.ordinal = ordinal;
        this.modules = modules;
        this.constData = constData;
        this.globalDynamicDataOffsets = globalDynamicDataOffsets;
        this.flatCachePrefills = flatCachePrefills;
        this.cache2dPrefills = cache2dPrefills;
        this.interpolatorPrefills = interpolatorPrefills;
        this.specializationConstants = specializationConstants;
        this.biomeMappings = biomeMappings;
        this.dumpedPath = dumpedPath;
    }

    public long getOrdinal() {
        return this.ordinal;
    }

    /**
     * The generated SPIR-V module for {@code type}, as a stream of 32-bit words. Feed to
     * {@code VkShaderModuleCreateInfo.pCode}.
     *
     * <p>There is one module per program type because glslang emits a single
     * {@code OpEntryPoint} per compilation, so the kernels cannot share one.</p>
     */
    public int[] getModule(SpirVGen.ProgramType type) {
        int[] words = this.modules.get(type);
        if (words == null) throw new IllegalStateException("No module generated for " + type);
        return words;
    }

    public EnumMap<SpirVGen.ProgramType, int[]> getModules() {
        return this.modules;
    }

    /**
     * Total size of all generated modules in bytes.
     */
    public int getGeneratedSizeBytes() {
        int size = 0;
        for (int[] words : this.modules.values()) size += words.length * Integer.BYTES;
        return size;
    }

    public byte[] getConstData() {
        return this.constData;
    }

    public Reference2IntLinkedOpenHashMap<Object> getGlobalDynamicDataOffsets() {
        return this.globalDynamicDataOffsets;
    }

    public int getFlatCachePrefills() {
        return this.flatCachePrefills;
    }

    public int getInterpolatorPrefills() {
        return this.interpolatorPrefills;
    }

    public int getCache2dPrefills() {
        return this.cache2dPrefills;
    }

    /**
     * Values the precompiled prelude consumes as {@code OpSpecConstant}s.
     *
     * <p>Keyed by {@code SpecId}, ready to hand to {@code VkSpecializationInfo}. Replaces the
     * OpenCL backend's {@code -D} preprocessor defines: the prelude is compiled at build time
     * and so cannot have per-world constants baked in. Constants used only by generated code
     * are emitted as plain {@code OpConstant} instead and never appear here.</p>
     */
    public Int2IntOpenHashMap getSpecializationConstants() {
        return this.specializationConstants;
    }

    public RegistryEntry<Biome>[] getBiomeMappings() {
        return this.biomeMappings;
    }

    public Path getDumpedPath() {
        return this.dumpedPath;
    }

}
