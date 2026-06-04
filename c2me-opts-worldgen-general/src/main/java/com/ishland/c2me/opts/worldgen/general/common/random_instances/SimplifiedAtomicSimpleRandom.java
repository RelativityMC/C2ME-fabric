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

package com.ishland.c2me.opts.worldgen.general.common.random_instances;

import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;

public class SimplifiedAtomicSimpleRandom extends CheckedRandom { // TODO [VanillaCopy]
    private static final int INT_BITS = 48;
    private static final long SEED_MASK = 281474976710655L;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private long seed;

    public SimplifiedAtomicSimpleRandom(long seed) {
        super(0);
        this.setSeed(seed);
    }

    @Override
    public Random split() {
        return new LocalRandom(this.nextLong());
    }

    @Override
    public net.minecraft.util.math.random.RandomSplitter nextSplitter() {
        return new CheckedRandom.Splitter(this.nextLong());
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
