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
