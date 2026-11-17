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

package com.ishland.c2me.opts.allocs.common;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.gen.sampler.SampleBuffer;
import net.minecraft.world.gen.sampler.SampleBufferPool;

import java.lang.ref.WeakReference;
import java.util.BitSet;
import java.util.function.IntFunction;

public class ObjectCachingUtils {

    private static final IntFunction<BitSet> bitSetConstructor = BitSet::new;

    public static final ThreadLocal<Int2ObjectOpenHashMap<BitSet>> BITSETS = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);
    public static final ThreadLocal<WeakReference<SampleBufferPool>> SAMPLE_POOL = ThreadLocal.withInitial(() -> null);

    public static final ScopedValue<IntFunction<SampleBuffer>> POOLED_SAMPLE_BUFFER_ALLOCATOR = ScopedValue.newInstance();

    private ObjectCachingUtils() {
    }

    public static BitSet getCachedOrNewBitSet(int bits) {
        final BitSet bitSet = BITSETS.get().computeIfAbsent(bits, bitSetConstructor);
        bitSet.clear();
        return bitSet;
    }

    private static SampleBufferPool createSampleBufferPool() {
        return new SampleBufferPool(32);
    }

    public static SampleBufferPool borrowCachedOrNewSampleBufferPool() {
        WeakReference<SampleBufferPool> weakReference = SAMPLE_POOL.get();
        SampleBufferPool pool = weakReference != null ? weakReference.get() : null;
        SAMPLE_POOL.remove();
        if (pool != null) {
            return pool;
        }
        return createSampleBufferPool();
    }

    public static void returnCachedSampleBufferPool(SampleBufferPool pool) {
        SAMPLE_POOL.set(new WeakReference<>(pool));
    }

}
