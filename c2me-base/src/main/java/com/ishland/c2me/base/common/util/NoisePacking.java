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

package com.ishland.c2me.base.common.util;

import java.util.Objects;

public class NoisePacking {

    public static final float[] FLAT_SIMPLEX_GRAD_F32 = new float[]{
            1, 1, 0, 0,
            -1, 1, 0, 0,
            1, -1, 0, 0,
            -1, -1, 0, 0,
            1, 0, 1, 0,
            -1, 0, 1, 0,
            1, 0, -1, 0,
            -1, 0, -1, 0,
            0, 1, 1, 0,
            0, -1, 1, 0,
            0, 1, -1, 0,
            0, -1, -1, 0,
            1, 1, 0, 0,
            0, -1, 1, 0,
            -1, 1, 0, 0,
            0, -1, -1, 0,
    };

    public static final int[] FLAT_SIMPLEX_GRAD_I8 = {
            0x00000101, // { 1, 1, 0}
            0x000001FF, // {-1, 1, 0}
            0x0000FF01, // { 1,-1, 0}
            0x0000FFFF, // {-1,-1, 0}
            0x00010001, // { 1, 0, 1}
            0x000100FF, // {-1, 0, 1}
            0x00FF0001, // { 1, 0,-1}
            0x00FF00FF, // {-1, 0,-1}
            0x00010100, // { 0, 1, 1}
            0x0001FF00, // { 0,-1, 1}
            0x00FF0100, // { 0, 1,-1}
            0x00FFFF00, // { 0,-1,-1}
            0x00000101, // { 1, 1, 0}
            0x0001FF00, // { 0,-1, 1}
            0x000001FF, // {-1, 1, 0}
            0x00FFFF00  // { 0,-1,-1}
    };

    public static int[] packPermutation512b(byte[] data) {
        Objects.requireNonNull(data);
        if (data.length != 256) {
            throw new IllegalArgumentException();
        }
        int[] ints = new int[256 / 4 * 2];
        for (int i = 0; i < 128; i++) {
            ints[i] = (data[(2 * i) & 0xff] & 0xff)
                    | ((data[(2 * i + 1) & 0xff] & 0xff) << 8)
                    | ((data[(2 * i + 1) & 0xff] & 0xff) << 16)
                    | ((data[(2 * i + 2) & 0xff] & 0xff) << 24);
        }
        return ints;
    }

    public static short[] packPermutation512bShort(byte[] data) {
        Objects.requireNonNull(data);
        if (data.length != 256) {
            throw new IllegalArgumentException();
        }
        short[] shorts = new short[256];
        for (int i = 0; i < 256; ++i) {
            shorts[i] = (short)((data[i] & 0xff) | ((data[(i + 1) & 0xff] & 0xff) << 8));
        }
        return shorts;
    }

    public static char[] packPermutation512bChar(byte[] data) {
        Objects.requireNonNull(data);
        if (data.length != 256) {
            throw new IllegalArgumentException();
        }
        char[] shorts = new char[256];
        for (int i = 0; i < 256; ++i) {
            shorts[i] = (char)((data[i] & 0xff) | ((data[(i + 1) & 0xff] & 0xff) << 8));
        }
        return shorts;
    }

    public static int[] packPermutation256b(byte[] data) {
        Objects.requireNonNull(data);
        if (data.length != 256) {
            throw new IllegalArgumentException();
        }
        int[] ints = new int[256 / 4];
        for (int i = 0; i < data.length; i++) {
            ints[i >> 2] |= (data[i] & 0xff) << ((i & 3) << 3);
        }
        return ints;
    }

    public static int indexPacked512bPermutation(int[] permutations, int index) {
        final int point = index & 0xFF;
        final int k = (point >> 1);
        final int perm_read = permutations[k];
        final int shift = (point & 1) << 4;
        return (perm_read >>> shift) & 0xffff;
    }

    public static int indexPacked512bPermutation(short[] permutations, int index) {
        return permutations[index & 0xff];
    }

    /**
     * Builds the fused perlin table: one entry per index holding both permutation
     * values and both 5-bit dot codes needed by the scalar kernels.
     * <p>
     * table[i] = perm[i] | (perm[(i + 1) & 255] << 8) | (dotCode(perm[i] & 15) << 16) | (dotCode(perm[(i + 1) & 255] & 15) << 24)
     */
    public static int[] buildFusedPermutationTable(byte[] data) {
        Objects.requireNonNull(data);
        if (data.length != 256) {
            throw new IllegalArgumentException();
        }
        int[] table = new int[256];
        for (int i = 0; i < 256; i++) {
            int v0 = data[i] & 0xFF;
            int v1 = data[(i + 1) & 0xFF] & 0xFF;
            table[i] = v0 | (v1 << 8)
                    | (dotCode(v0) << 16)
                    | (dotCode(v1) << 24);
        }
        return table;
    }

    private static int dotCode(int permValue) {
        int h = permValue & 15;
        return (h < 8 ? 1 : 0)
                | (h < 4 ? 2 : 0)
                | (h == 12 || h == 14 ? 4 : 0)
                | ((h & 3) << 3);
    }

    public static int indexPacked256bPermutation(int[] permutations, int index) {
        final int point = index & 0xFF;
        return (permutations[point >>> 2] >> ((index & 3) << 3)) & 0xFF;
    }

    public static int gradX(int packed) {
        return (byte) packed;
    }

    public static int gradY(int packed) {
        return (byte) (packed >>> 8);
    }

    public static int gradZ(int packed) {
        return (byte) (packed >>> 16);
    }

}
