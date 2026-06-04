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

package com.ishland.c2me.opts.dfc.common.util;

import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
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
