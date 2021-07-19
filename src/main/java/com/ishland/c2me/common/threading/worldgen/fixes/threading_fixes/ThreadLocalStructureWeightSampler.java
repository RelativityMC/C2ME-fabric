package com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes;

import com.ishland.c2me.mixin.access.IStructureWeightSampler;
import net.minecraft.world.gen.StructureWeightSampler;

public class ThreadLocalStructureWeightSampler extends StructureWeightSampler {

    private final ThreadLocal<StructureWeightSampler> structureWeightSamplerThreadLocal;

    public ThreadLocalStructureWeightSampler() {
        this.structureWeightSamplerThreadLocal = ThreadLocal.withInitial(StructureWeightSampler::new);
    }

    @Override
    protected double getWeight(int x, int y, int z) {
        return ((IStructureWeightSampler) structureWeightSamplerThreadLocal.get()).invokeGetWeight(x, y, z);
    }
}
