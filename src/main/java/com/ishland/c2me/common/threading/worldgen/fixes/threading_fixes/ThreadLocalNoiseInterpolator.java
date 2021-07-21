package com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.NoiseInterpolator;

public class ThreadLocalNoiseInterpolator extends NoiseInterpolator {

    private final ThreadLocal<NoiseInterpolator> noiseInterpolatorThreadLocal;

    public ThreadLocalNoiseInterpolator(int sizeX, int sizeY, int sizeZ, ChunkPos pos, int minY, ColumnSampler columnSampler) {
        super(sizeX, sizeY, sizeZ, pos, minY, columnSampler);
        this.noiseInterpolatorThreadLocal = ThreadLocal.withInitial(() -> new NoiseInterpolator(sizeX, sizeY, sizeZ, pos, minY, columnSampler));
    }

    @Override
    public void sampleStartNoise() {
        this.noiseInterpolatorThreadLocal.get().sampleStartNoise();
    }

    @Override
    public void sampleEndNoise(int x) {
        this.noiseInterpolatorThreadLocal.get().sampleEndNoise(x);
    }

    @Override
    public void sampleNoiseCorners(int noiseY, int noiseZ) {
        this.noiseInterpolatorThreadLocal.get().sampleNoiseCorners(noiseY, noiseZ);
    }

    @Override
    public void sampleNoiseY(double deltaY) {
        this.noiseInterpolatorThreadLocal.get().sampleNoiseY(deltaY);
    }

    @Override
    public void sampleNoiseX(double deltaX) {
        this.noiseInterpolatorThreadLocal.get().sampleNoiseX(deltaX);
    }

    @Override
    public double sampleNoise(double deltaZ) {
        return this.noiseInterpolatorThreadLocal.get().sampleNoise(deltaZ);
    }

    @Override
    public void swapBuffers() {
        this.noiseInterpolatorThreadLocal.get().swapBuffers();
    }
}
