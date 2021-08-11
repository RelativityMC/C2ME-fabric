package com.ishland.c2me.common.fixes.worldgen.threading;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.SurfaceConfig;

import java.util.Random;
import java.util.function.Supplier;

public class ThreadLocalSurfaceBuilder<C extends SurfaceConfig> extends SurfaceBuilder<C> {

    private final ThreadLocal<SurfaceBuilder<C>> surfaceBuilderThreadLocal;

    public ThreadLocalSurfaceBuilder(Supplier<SurfaceBuilder<C>> supplier, Codec<C> codec) {
        super(codec);
        this.surfaceBuilderThreadLocal = ThreadLocal.withInitial(supplier);
    }

    @Override
    public void initSeed(long seed) {
        this.surfaceBuilderThreadLocal.get().initSeed(seed);
    }

    @Override
    public void generate(Random random, Chunk chunk, Biome biome, int x, int z, int height, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, int i, long l, C surfaceConfig) {
        this.surfaceBuilderThreadLocal.get().generate(random, chunk, biome, x, z, height, noise, defaultBlock, defaultFluid, seaLevel, i, l, surfaceConfig);
    }
}
