package com.ishland.c2me.common.fixes.worldgen.threading;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureWeightSampler;

public class ThreadLocalStructureWeightSampler extends StructureWeightSampler {

    private final ThreadLocal<StructureWeightSampler> structureWeightSamplerThreadLocal;

    public ThreadLocalStructureWeightSampler(StructureAccessor structureAccessor, Chunk chunk) {
        super(structureAccessor, chunk);
        this.structureWeightSamplerThreadLocal = ThreadLocal.withInitial(() -> new StructureWeightSampler(structureAccessor, chunk));
    }

    @Override
    public double calculateNoise(int i, int j, int k) {
        return this.structureWeightSamplerThreadLocal.get().calculateNoise(i, j, k);
    }
}
