package com.ishland.c2me.opts.worldgen.general.common.random_instances;

import net.minecraft.world.gen.random.AbstractRandom;
import net.minecraft.world.gen.random.AtomicSimpleRandom;
import net.minecraft.world.gen.random.SimpleRandom;

public class SimplifiedAtomicSimpleRandom extends AtomicSimpleRandom { // TODO [VanillaCopy]
    private static final int INT_BITS = 48;
    private static final long SEED_MASK = 281474976710655L;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private long seed;

    public SimplifiedAtomicSimpleRandom(long seed) {
        super(0);
        this.seed = seed;
    }

    @Override
    public AbstractRandom derive() {
        return new SimpleRandom(this.nextLong());
    }

    @Override
    public net.minecraft.world.gen.random.RandomDeriver createRandomDeriver() {
        return new AtomicSimpleRandom.RandomDeriver(this.nextLong());
    }

    @Override
    public void setSeed(long l) {
        this.seed = (l ^ MULTIPLIER) & SEED_MASK;
    }

    @Override
    public int next(int bits) {
        long l = this.seed * MULTIPLIER + INCREMENT & SEED_MASK;
        this.seed = l;
        return (int)(l >> INT_BITS - bits);
    }
}
