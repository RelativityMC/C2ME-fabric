package com.ishland.c2me.opts.dfc.common.ast;

import com.ishland.c2me.opts.dfc.common.vif.EachApplierVanillaInterface;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;

public enum EvalType {
    NORMAL, INTERPOLATION;

    public static EvalType from(DensityFunction.NoisePos pos) {
        return pos instanceof ChunkNoiseSampler ? INTERPOLATION : NORMAL;
    }

    public static EvalType from(DensityFunction.EachApplier applier) {
        if (applier instanceof EachApplierVanillaInterface vif) {
            return vif.getType();
        }
        return applier instanceof ChunkNoiseSampler ? INTERPOLATION : NORMAL;
    }
}
