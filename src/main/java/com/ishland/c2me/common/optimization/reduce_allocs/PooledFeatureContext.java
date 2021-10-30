package com.ishland.c2me.common.optimization.reduce_allocs;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.Random;

public class PooledFeatureContext<FC extends FeatureConfig> extends FeatureContext<FC> {

    public static final ThreadLocal<SimpleObjectPool<PooledFeatureContext<?>>> POOL = ThreadLocal.withInitial(() -> new SimpleObjectPool<>(unused -> new PooledFeatureContext<>(), unused -> {}, 2048));

    private StructureWorldAccess world;
    private ChunkGenerator generator;
    private Random random;
    private BlockPos origin;
    private FC config;

    public PooledFeatureContext() {
        super(null, null, null, null, null);
    }

    public void reInit(StructureWorldAccess world, ChunkGenerator generator, Random random, BlockPos origin, FC config) {
        this.world = world;
        this.generator = generator;
        this.random = random;
        this.origin = origin;
        this.config = config;
    }

    public StructureWorldAccess getWorld() {
        return this.world;
    }

    public ChunkGenerator getGenerator() {
        return this.generator;
    }

    public Random getRandom() {
        return this.random;
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public FC getConfig() {
        return this.config;
    }
}
