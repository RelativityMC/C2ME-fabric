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

package com.ishland.c2me.notickvd.common.iterators;

import net.minecraft.util.math.ChunkPos;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SpiralIterator implements ChunkIterator {

    private static final int RIGHT = 0, DOWN = 1, LEFT = 2, UP = 3;
    private final int originX, originZ;
    private final int radius;
    private final long total;
    private boolean needStep = false;
    private int x, z;
    private int spanTotal = 1, spanCount = 0, spanProgress = 0;
    private int direction = RIGHT;
    private long currentIndex;

    public SpiralIterator(int originX, int originZ, int radius) {
        this.originX = originX;
        this.originZ = originZ;
        this.radius = radius;
        this.x = originX;
        this.z = originZ;
        this.total = (radius * 2L + 1) * (radius * 2L + 1);
    }

    @Override
    public boolean hasNext() {
        return x != this.originX + this.radius || z != this.originZ + this.radius;
    }

    @Override
    public ChunkPos next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        if (this.needStep) {
            switch (this.direction) {
                case RIGHT -> this.x ++;
                case DOWN -> this.z --;
                case LEFT -> this.x --;
                case UP -> this.z ++;
                default -> throw new AssertionError();
            }
            this.spanProgress ++;
            if (this.spanProgress == this.spanTotal) {
                this.spanProgress = 0;
                this.spanCount ++;
                if (this.spanCount >= 2) {
                    this.spanTotal ++;
                    this.spanCount = 0;
                }
                this.direction ++;
                this.direction %= 4;
            }
        }
        this.needStep = true;

        this.currentIndex ++;

        return new ChunkPos(this.x, this.z);
    }

    @Override
    public long remaining() {
        return this.total - this.currentIndex;
    }

    @Override
    public long total() {
        return this.total;
    }

    @Override
    public int originX() {
        return this.originX;
    }

    @Override
    public int originZ() {
        return this.originZ;
    }

    @Override
    public int radius() {
        return this.radius;
    }
}
