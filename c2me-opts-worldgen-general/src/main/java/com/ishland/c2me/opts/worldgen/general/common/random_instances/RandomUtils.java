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

import com.ishland.c2me.base.mixin.access.IAtomicSimpleRandomDeriver;
import com.ishland.c2me.base.mixin.access.ISimpleRandom;
import com.ishland.c2me.base.mixin.access.IXoroshiro128PlusPlusRandom;
import com.ishland.c2me.base.mixin.access.IXoroshiro128PlusPlusRandomDeriver;
import com.ishland.c2me.base.mixin.access.IXoroshiro128PlusPlusRandomImpl;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;

public class RandomUtils {

    private static final ThreadLocal<Xoroshiro128PlusPlusRandom> xoroshiro = ThreadLocal.withInitial(() -> new Xoroshiro128PlusPlusRandom(0L, 0L));
    private static final ThreadLocal<LocalRandom> simple = ThreadLocal.withInitial(() -> new LocalRandom(0L));

    public static void derive(RandomSplitter deriver, Random random, int x, int y, int z) {
        if (deriver instanceof Xoroshiro128PlusPlusRandom.Splitter) {
            final IXoroshiro128PlusPlusRandomImpl implementation = (IXoroshiro128PlusPlusRandomImpl) ((IXoroshiro128PlusPlusRandom) random).getImplementation();
            final IXoroshiro128PlusPlusRandomDeriver deriver1 = (IXoroshiro128PlusPlusRandomDeriver) deriver;
            implementation.setSeedLo(MathHelper.hashCode(x, y, z) ^ deriver1.getSeedLo());
            implementation.setSeedHi(deriver1.getSeedHi());
            return;
        }
        if (deriver instanceof CheckedRandom.Splitter) {
            final ISimpleRandom random1 = (ISimpleRandom) random;
            final IAtomicSimpleRandomDeriver deriver1 = (IAtomicSimpleRandomDeriver) deriver;
            random1.invokeSetSeed(MathHelper.hashCode(x, y, z) ^ deriver1.getSeed());
            return;
        }
        throw new IllegalArgumentException();
    }

    public static Random getThreadLocalRandom(RandomSplitter deriver) {
        if (deriver instanceof Xoroshiro128PlusPlusRandom.Splitter) {
            return xoroshiro.get();
        }
        if (deriver instanceof CheckedRandom.Splitter) {
            return simple.get();
        }
        throw new IllegalArgumentException();
    }

    public static Random getRandom(RandomSplitter deriver) {
        if (deriver instanceof Xoroshiro128PlusPlusRandom.Splitter) {
            return new Xoroshiro128PlusPlusRandom(0L, 0L);
        }
        if (deriver instanceof CheckedRandom.Splitter) {
            return new LocalRandom(0L);
        }
        throw new IllegalArgumentException();
    }

}
