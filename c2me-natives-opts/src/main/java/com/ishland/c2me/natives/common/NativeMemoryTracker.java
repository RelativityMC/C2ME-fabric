package com.ishland.c2me.natives.common;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.LongAdder;

import static sun.misc.Unsafe.ADDRESS_SIZE;

public class NativeMemoryTracker {

    private static final Cleaner CLEANER = Cleaner.create(new ThreadFactoryBuilder().setNameFormat("C2ME Natives Cleaner").setDaemon(true).build());

    private static final LongAdder allocatedBytes = new LongAdder();

    public static void registerAllocatedMemory(Object o, long size, long ptr) {
        Preconditions.checkArgument(size > 0, "size");
        final long actualSize = alignToHeapWordSize(size);
        allocatedBytes.add(actualSize);
        CLEANER.register(o, () -> {
            UnsafeUtil.getInstance().freeMemory(ptr);
            allocatedBytes.add(-actualSize);
        });
    }

    public static long allocateMemory(Object o, long size) {
        Preconditions.checkArgument(size > 0, "size");
        final long ptr = UnsafeUtil.getInstance().allocateMemory(size);
        registerAllocatedMemory(o, size, ptr);
        return ptr;
    }

    public static long allocateMemoryWithoutCleaner(long size) {
        final long actualSize = alignToHeapWordSize(size);
        allocatedBytes.add(actualSize);
        return UnsafeUtil.getInstance().allocateMemory(size);
    }

    public static void registerAllocatedMemoryWithoutCleaner(long size) {
        final long actualSize = alignToHeapWordSize(size);
        allocatedBytes.add(actualSize);
    }

    public static void freeMemoryWithoutCleaner(long ptr, long size) {
        if (ptr == 0) throw new NullPointerException();
        final long actualSize = alignToHeapWordSize(size);
        allocatedBytes.add(-actualSize);
        UnsafeUtil.getInstance().freeMemory(ptr);
    }

    public static long getAllocatedBytes() {
        return allocatedBytes.sum();
    }

    private static long alignToHeapWordSize(long bytes) {
        return (bytes + ADDRESS_SIZE - 1) & ~(ADDRESS_SIZE - 1);
    }

}
