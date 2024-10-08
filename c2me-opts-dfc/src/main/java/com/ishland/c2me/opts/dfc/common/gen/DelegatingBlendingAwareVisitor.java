package com.ishland.c2me.opts.dfc.common.gen;

import com.ishland.c2me.opts.dfc.common.ducks.IBlendingAwareVisitor;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Objects;

public class DelegatingBlendingAwareVisitor implements IBlendingAwareVisitor, DensityFunction.DensityFunctionVisitor {

    private final DensityFunction.DensityFunctionVisitor delegate;
    private final boolean blendingEnabled;

    public DelegatingBlendingAwareVisitor(DensityFunction.DensityFunctionVisitor delegate, boolean blendingEnabled) {
        this.delegate = Objects.requireNonNull(delegate);
        this.blendingEnabled = blendingEnabled;
    }

    @Override
    public DensityFunction apply(DensityFunction densityFunction) {
        return this.delegate.apply(densityFunction);
    }

    @Override
    public DensityFunction.Noise apply(DensityFunction.Noise noiseDensityFunction) {
        return this.delegate.apply(noiseDensityFunction);
    }

    @Override
    public boolean c2me$isBlendingEnabled() {
        return this.blendingEnabled;
    }
}
