package com.ishland.c2me.fixes.worldgen.threading_issues.common;

import net.minecraft.util.math.random.LocalRandom;

import java.util.ConcurrentModificationException;
import java.util.function.Supplier;

public class CheckedThreadLocalRandom extends LocalRandom {

    private final Supplier<Thread> owner;

    public CheckedThreadLocalRandom(long seed, Supplier<Thread> owner) {
        super(seed);
        this.owner = owner;
    }

    @Override
    public void setSeed(long seed) {
        if (Thread.currentThread() != this.owner.get()) throw new ConcurrentModificationException();
        super.setSeed(seed);
    }

    @Override
    public int next(int bits) {
        if (Thread.currentThread() != this.owner.get()) throw new ConcurrentModificationException();
        return super.next(bits);
    }
}
