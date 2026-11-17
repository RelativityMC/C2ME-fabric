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

package com.ishland.c2me.base.common.util;

public class MemoryUtil {

    public static int[] byte2int(byte[] data) {
        if (data == null) return null;
        int[] ints = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            ints[i] = data[i] & 0xff;
        }
        return ints;
    }

    public static int[] packByte2int(byte[] data) {
        if (data == null) return null;
        int[] ints = new int[Math.ceilDiv(data.length, 4)];
        for (int i = 0; i < data.length; i++) {
            ints[i >> 2] |= (data[i] & 0xff) << ((i & 3) << 3);
        }
        return ints;
    }

    public static int roundUp(int num, int base) {
        int temp = num % base;
        if (temp < 0)
            temp = base + temp;
        if (temp == 0)
            return num;
        return num + base - temp;
    }

    public static long roundUp(long num, long base) {
        long temp = num % base;
        if (temp < 0)
            temp = base + temp;
        if (temp == 0)
            return num;
        return num + base - temp;
    }

}
