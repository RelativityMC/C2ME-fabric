package com.ishland.c2me.rewrites.chunkio.common;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import sun.misc.Unsafe;
import java.lang.reflect.Field;

public class OffHeapChunkCache {
    private static final Unsafe UNSAFE;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final Long2LongOpenHashMap addressMap = new Long2LongOpenHashMap();
    private final Long2LongOpenHashMap sizeMap = new Long2LongOpenHashMap();

    public synchronized void put(long pos, byte[] data) {
        if (addressMap.containsKey(pos)) {
            UNSAFE.freeMemory(addressMap.get(pos));
        }
        long address = UNSAFE.allocateMemory(data.length);
        for (int i = 0; i < data.length; i++) {
            UNSAFE.putByte(address + i, data[i]);
        }
        addressMap.put(pos, address);
        sizeMap.put(pos, (long) data.length);
    }

    public synchronized byte[] get(long pos) {
        if (!addressMap.containsKey(pos)) return null;
        long address = addressMap.get(pos);
        int size = (int) sizeMap.get(pos);
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = UNSAFE.getByte(address + i);
        }
        return data;
    }

    public synchronized void remove(long pos) {
        if (addressMap.containsKey(pos)) {
            UNSAFE.freeMemory(addressMap.remove(pos));
            sizeMap.remove(pos);
        }
    }

    public synchronized void clear() {
        for (long address : addressMap.values()) {
            UNSAFE.freeMemory(address);
        }
        addressMap.clear();
        sizeMap.clear();
    }
}
