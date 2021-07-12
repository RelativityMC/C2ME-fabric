package com.ishland.c2me.compatibility.common.terra;

import com.dfsek.terra.api.math.noise.NoiseSampler;
import com.dfsek.terra.api.math.paralithic.noise.NoiseFunction2;

public final class ThreadLocalNoiseFunction2 extends NoiseFunction2 {
    private final ThreadLocal<NoiseFunction2> delegate;

    public ThreadLocalNoiseFunction2(NoiseSampler gen) {
        super(null);
        this.delegate = ThreadLocal.withInitial(() -> new NoiseFunction2(gen));
    }

    @Override
    public double eval(double... args) {
        return delegate.get().eval(args);
    }

    @Override
    public int getArgNumber() {
        return delegate.get().getArgNumber();
    }

    @Override
    public boolean isStateless() {
        return delegate.get().isStateless();
    }

}
