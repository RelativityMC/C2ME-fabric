package com.ishland.c2me.opts.allocs.common;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ObjectCachingUtils {

    private static final IntFunction<BitSet> bitSetConstructor = BitSet::new;
    private static final IntFunction<double[]> doubleArrayConstructor = double[]::new;
    private static final IntFunction<int[]> intArrayConstructor = int[]::new;
    private static final IntFunction<float[]> floatArrayConstructor = float[]::new;
    private static final IntFunction<long[]> longArrayConstructor = long[]::new;
    private static final IntFunction<ArrayList<?>> arrayListConstructor = size -> new ArrayList<>(size);
    private static final IntFunction<LinkedList<?>> linkedListConstructor = size -> new LinkedList<>();
    private static final IntFunction<HashMap<?, ?>> hashMapConstructor = size -> new HashMap<>(size);

    public static ThreadLocal<Int2ObjectOpenHashMap<BitSet>> BITSETS = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);
    public static ThreadLocal<Int2ObjectOpenHashMap<double[]>> DOUBLE_ARRAYS = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);
    public static ThreadLocal<Int2ObjectOpenHashMap<int[]>> INT_ARRAYS = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);
    public static ThreadLocal<Int2ObjectOpenHashMap<float[]>> FLOAT_ARRAYS = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);
    public static ThreadLocal<Int2ObjectOpenHashMap<long[]>> LONG_ARRAYS = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);
    public static ThreadLocal<Int2ObjectOpenHashMap<ArrayList<?>>> ARRAY_LISTS = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);
    public static ThreadLocal<Int2ObjectOpenHashMap<LinkedList<?>>> LINKED_LISTS = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);
    public static ThreadLocal<Int2ObjectOpenHashMap<HashMap<?, ?>>> HASH_MAPS = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);

    // Object pools for density function related objects
    private static final ThreadLocal<Object2ObjectOpenHashMap<Class<?>, LinkedList<Object>>> OBJECT_POOLS =
        ThreadLocal.withInitial(Object2ObjectOpenHashMap::new);

    private ObjectCachingUtils() {
    }

    public static BitSet getCachedOrNewBitSet(int bits) {
        final BitSet bitSet = BITSETS.get().computeIfAbsent(bits, bitSetConstructor);
        bitSet.clear();
        return bitSet;
    }

    public static double[] getCachedOrNewDoubleArray(int length) {
        final double[] array = DOUBLE_ARRAYS.get().computeIfAbsent(length, doubleArrayConstructor);
        return array;
    }

    public static int[] getCachedOrNewIntArray(int length) {
        final int[] array = INT_ARRAYS.get().computeIfAbsent(length, intArrayConstructor);
        return array;
    }

    public static float[] getCachedOrNewFloatArray(int length) {
        final float[] array = FLOAT_ARRAYS.get().computeIfAbsent(length, floatArrayConstructor);
        return array;
    }

    public static long[] getCachedOrNewLongArray(int length) {
        final long[] array = LONG_ARRAYS.get().computeIfAbsent(length, longArrayConstructor);
        return array;
    }

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> getCachedOrNewArrayList(int initialCapacity) {
        final ArrayList<T> list = (ArrayList<T>) ARRAY_LISTS.get().computeIfAbsent(initialCapacity, arrayListConstructor);
        list.clear();
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T> LinkedList<T> getCachedOrNewLinkedList(int dummy) {
        final LinkedList<T> list = (LinkedList<T>) LINKED_LISTS.get().computeIfAbsent(dummy, linkedListConstructor);
        list.clear();
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> getCachedOrNewHashMap(int initialCapacity) {
        final HashMap<K, V> map = (HashMap<K, V>) HASH_MAPS.get().computeIfAbsent(initialCapacity, hashMapConstructor);
        map.clear();
        return map;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getPooledObject(Class<T> clazz, Supplier<T> constructor) {
        LinkedList<Object> pool = OBJECT_POOLS.get().computeIfAbsent(clazz, k -> new LinkedList<>());
        T obj = (T) pool.poll();
        if (obj == null) {
            obj = constructor.get();
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    public static void returnPooledObject(Class<?> clazz, Object obj) {
        if (obj != null) {
            LinkedList<Object> pool = OBJECT_POOLS.get().computeIfAbsent(clazz, k -> new LinkedList<>());
            if (pool.size() < 32) { // Limit pool size to prevent memory leaks
                pool.offer(obj);
            }
        }
    }

    /**
     * Arena-based memory allocator for temporary structures during world generation.
     * Provides fast allocation/deallocation for objects that have short lifetimes.
     */
    public static class GenerationArena {
        private static final ThreadLocal<GenerationArena> CURRENT_ARENA = new ThreadLocal<>();

        private final LinkedList<Object[]> allocations = new LinkedList<>();
        private int currentChunkIndex = 0;
        private Object[] currentChunk = new Object[1024];
        private boolean closed = false;

        public static GenerationArena get() {
            GenerationArena arena = CURRENT_ARENA.get();
            if (arena == null || arena.closed) {
                arena = new GenerationArena();
                CURRENT_ARENA.set(arena);
            }
            return arena;
        }

        @SuppressWarnings("unchecked")
        public <T> T allocate(Class<T> clazz, Supplier<T> constructor) {
            if (closed) {
                throw new IllegalStateException("Arena is closed");
            }

            // Try to reuse from pool first
            T obj = getPooledObject(clazz, constructor);

            // Track allocation for cleanup
            if (currentChunkIndex >= currentChunk.length) {
                allocations.add(currentChunk);
                currentChunk = new Object[1024];
                currentChunkIndex = 0;
            }
            currentChunk[currentChunkIndex++] = obj;

            return obj;
        }

        public void close() {
            if (!closed) {
                closed = true;
                // Return objects to pools
                for (Object[] chunk : allocations) {
                    for (int i = 0; i < chunk.length && chunk[i] != null; i++) {
                        returnPooledObject(chunk[i].getClass(), chunk[i]);
                    }
                }
                for (int i = 0; i < currentChunkIndex; i++) {
                    if (currentChunk[i] != null) {
                        returnPooledObject(currentChunk[i].getClass(), currentChunk[i]);
                    }
                }
                allocations.clear();
                currentChunk = null;
                currentChunkIndex = 0;
            }
        }

        // Auto-closeable for try-with-resources
        public static class ScopedArena implements AutoCloseable {
            private final GenerationArena arena;

            public ScopedArena() {
                this.arena = new GenerationArena();
                CURRENT_ARENA.set(arena);
            }

            @Override
            public void close() {
                arena.close();
            }

            public <T> T allocate(Class<T> clazz, Supplier<T> constructor) {
                return arena.allocate(clazz, constructor);
            }
        }
    }

}
