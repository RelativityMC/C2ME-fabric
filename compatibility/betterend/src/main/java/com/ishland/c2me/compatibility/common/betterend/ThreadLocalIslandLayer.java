package com.ishland.c2me.compatibility.common.betterend;

import ru.betterend.world.generator.IslandLayer;
import ru.betterend.world.generator.LayerOptions;

public class ThreadLocalIslandLayer extends IslandLayer {

    private final ThreadLocal<IslandLayer> delegate;

    public ThreadLocalIslandLayer(int seed, LayerOptions options) {
        super(seed, options);
        this.delegate = ThreadLocal.withInitial(() -> new IslandLayer(seed, options));
    }

    @Override
    public void updatePositions(double x, double z) {
        delegate.get().updatePositions(x, z);
    }

    @Override
    public float getDensity(double x, double y, double z) {
        return delegate.get().getDensity(x, y, z);
    }

    @Override
    public float getDensity(double x, double y, double z, float height) {
        return delegate.get().getDensity(x, y, z, height);
    }

    @Override
    public void clearCache() {
        delegate.get().clearCache();
    }
}
