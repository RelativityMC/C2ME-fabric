package com.ishland.c2me.opts.allocs.common;

import com.ishland.flowsched.structs.SimpleObjectPool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.Optional;

public class PooledFeatureContext<FC extends FeatureConfig> extends FeatureContext<FC> {

    public static final ThreadLocal<SimpleObjectPool<PooledFeatureContext<?>>> POOL = ThreadLocal.withInitial(() -> new SimpleObjectPool<>(unused -> new PooledFeatureContext<>(), PooledFeatureContext::reInit, PooledFeatureContext::reInit, 2048));

    private Optional<ConfiguredFeature<?, ?>> feature;
    private StructureWorldAccess world;
    private ChunkGenerator generator;
    private Random random;
    private BlockPos origin;
    private FC config;

    public PooledFeatureContext() {
        super(null, null, null, null, null, null);
    }

    public void reInit(Optional<ConfiguredFeature<?, ?>> feature, StructureWorldAccess world, ChunkGenerator generator, Random random, BlockPos origin, FC config) {
        this.feature = feature;
        this.world = world;
        this.generator = generator;
        this.random = random;
        this.origin = origin;
        this.config = config;
    }

    public void reInit() {
        this.feature = null;
        this.world = null;
        this.generator = null;
        this.random = null;
        this.origin = null;
        this.config = null;
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

    public Optional<ConfiguredFeature<?, ?>> getFeature() {
        return this.feature;
    }
}
