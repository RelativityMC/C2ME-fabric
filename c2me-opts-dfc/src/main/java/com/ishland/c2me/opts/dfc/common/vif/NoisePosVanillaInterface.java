package com.ishland.c2me.opts.dfc.common.vif;

import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Objects;

public class NoisePosVanillaInterface implements DensityFunction.NoisePos {

    private final int x;
    private final int y;
    private final int z;
    private final EvalType type;

    public NoisePosVanillaInterface(int x, int y, int z, EvalType type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public int blockX() {
        return x;
    }

    @Override
    public int blockY() {
        return y;
    }

    @Override
    public int blockZ() {
        return z;
    }

    public EvalType getType() {
        return type;
    }
}
