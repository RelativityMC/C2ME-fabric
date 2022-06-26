package com.ishland.c2me.fixes.worldgen.threading_issues.common;

import net.minecraft.util.math.random.LocalRandom;

import java.util.ConcurrentModificationException;

public class CheckedThreadLocalRandom extends LocalRandom {

    private final Thread owner;

    public CheckedThreadLocalRandom(long seed, Thread owner) {
        super(seed);
        this.owner = owner;
    }

    @Override
    public void setSeed(long seed) {
        if (this.owner != null && Thread.currentThread() != this.owner) throw new ConcurrentModificationException();
        super.setSeed(seed);
    }

    @Override
    public int next(int bits) {
        if (this.owner != null && Thread.currentThread() != this.owner) throw new ConcurrentModificationException();
        return super.next(bits);
    }
}
