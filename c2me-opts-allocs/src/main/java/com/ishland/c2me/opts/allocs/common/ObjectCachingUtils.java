package com.ishland.c2me.opts.allocs.common;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.BitSet;
import java.util.function.IntFunction;

public class ObjectCachingUtils {

    private static final IntFunction<BitSet> bitSetConstructor = BitSet::new;

    public static ThreadLocal<Int2ObjectOpenHashMap<BitSet>> BITSETS = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);

    private ObjectCachingUtils() {
    }

    public static BitSet getCachedOrNewBitSet(int bits) {
        final BitSet bitSet = BITSETS.get().computeIfAbsent(bits, bitSetConstructor);
        bitSet.clear();
        return bitSet;
    }

}
