package com.ishland.c2me.opts.dfc.common.vif;

import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ducks.IArrayCacheCapable;
import com.ishland.c2me.opts.dfc.common.util.ArrayCache;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Objects;

public class NoisePosVanillaInterface implements DensityFunction.NoisePos, IArrayCacheCapable {

    private final int x;
    private final int y;
    private final int z;
    private final EvalType type;
    private final ArrayCache cache;

    public NoisePosVanillaInterface(int x, int y, int z, EvalType type) {
        this(x, y, z, type, new ArrayCache());
    }

    public NoisePosVanillaInterface(int x, int y, int z, EvalType type, ArrayCache cache) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = Objects.requireNonNull(type);
        this.cache = Objects.requireNonNull(cache);
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

    @Override
    public ArrayCache c2me$getArrayCache() {
        return this.cache;
    }
}
