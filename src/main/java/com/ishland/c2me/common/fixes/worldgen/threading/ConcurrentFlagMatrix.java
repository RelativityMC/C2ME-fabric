package com.ishland.c2me.common.fixes.worldgen.threading;

import net.minecraft.structure.WoodlandMansionGenerator;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentFlagMatrix extends WoodlandMansionGenerator.FlagMatrix {

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public ConcurrentFlagMatrix(int n, int m, int fallback) {
        super(n, m, fallback);
    }

    @Override
    public void set(int i, int j, int value) {
        rwLock.writeLock().lock();
        try {
            super.set(i, j, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void fill(int i0, int j0, int i1, int j1, int value) {
        rwLock.writeLock().lock();
        try {
            super.fill(i0, j0, i1, j1, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public int get(int i, int j) {
        rwLock.readLock().lock();
        try {
            return super.get(i, j);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void update(int i, int j, int expected, int newValue) {
        // semi-VanillaCopy
        if (this.get(i, j) == expected) {
            this.set(i, j, newValue);
        }
    }

    @Override
    public boolean anyMatchAround(int i, int j, int value) {
        rwLock.readLock().lock();
        try {
            return super.anyMatchAround(i, j, value);
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
