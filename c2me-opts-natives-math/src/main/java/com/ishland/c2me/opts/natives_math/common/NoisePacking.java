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

package com.ishland.c2me.opts.natives_math.common;

import java.util.Objects;

public class NoisePacking {

    public static int[] packPermutation0(byte[] data) {
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

}
