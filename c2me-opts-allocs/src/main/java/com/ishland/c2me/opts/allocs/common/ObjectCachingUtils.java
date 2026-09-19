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
