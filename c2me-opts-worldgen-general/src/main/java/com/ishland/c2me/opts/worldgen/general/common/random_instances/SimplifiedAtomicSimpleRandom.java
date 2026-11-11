/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
