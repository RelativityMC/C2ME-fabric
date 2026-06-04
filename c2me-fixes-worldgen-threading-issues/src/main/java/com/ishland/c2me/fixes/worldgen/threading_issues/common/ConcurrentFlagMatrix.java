/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.fixes.worldgen.threading_issues.common;

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
