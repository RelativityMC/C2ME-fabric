package com.ishland.c2me.fixes.worldgen.threading_issues.common;

import net.minecraft.util.math.random.LocalRandom;

import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.function.Supplier;

public class CheckedThreadLocalRandom extends LocalRandom {

    private static final String MESSAGE = "ThreadLocalRandom accessed from a different thread (owner: %s, current: %s)";

    private final Supplier<Thread> owner;

    public CheckedThreadLocalRandom(long seed, Supplier<Thread> owner) {
        super(seed);
        this.owner = Objects.requireNonNull(owner);
    }

    @Override
    public void setSeed(long seed) {
        Thread owner = this.owner != null ? this.owner.get() : null;
        if (owner != null && Thread.currentThread() != owner)
            throw new ConcurrentModificationException(MESSAGE.formatted(owner, Thread.currentThread()));
        super.setSeed(seed);
    }

    @Override
    public int next(int bits) {
        Thread owner = this.owner != null ? this.owner.get() : null;
        if (owner != null && Thread.currentThread() != owner)
            throw new ConcurrentModificationException(MESSAGE.formatted(owner, Thread.currentThread()));
        return super.next(bits);
    }
}
