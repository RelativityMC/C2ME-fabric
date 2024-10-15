package com.ishland.c2me.opts.dfc.common.util;

import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.util.Arrays;

public class ArrayCache {

    private final Int2ReferenceArrayMap<ReferenceArrayList<double[]>> doubleArrayCache = new Int2ReferenceArrayMap<>();
    private final Int2ReferenceArrayMap<ReferenceArrayList<int[]>> intArrayCache = new Int2ReferenceArrayMap<>();

    public double[] getDoubleArray(int size, boolean zero) {
        ReferenceArrayList<double[]> list = this.doubleArrayCache.computeIfAbsent(size, k -> new ReferenceArrayList<>());
        if (list.isEmpty()) {
            return new double[size];
        } else {
            double[] popped = list.pop();
            if (zero) {
                Arrays.fill(popped, 0.0);
            }
            return popped;
        }
    }

    public int[] getIntArray(int size, boolean zero) {
        ReferenceArrayList<int[]> list = this.intArrayCache.computeIfAbsent(size, k -> new ReferenceArrayList<>());
        if (list.isEmpty()) {
            return new int[size];
        } else {
            int[] popped = list.pop();
            if (zero) {
                Arrays.fill(popped, 0);
            }
            return popped;
        }
    }

    public void recycle(double[] array) {
        this.doubleArrayCache.computeIfAbsent(array.length, k -> new ReferenceArrayList<>()).add(array);
    }

    public void recycle(int[] array) {
        this.intArrayCache.computeIfAbsent(array.length, k -> new ReferenceArrayList<>()).add(array);
    }

}
