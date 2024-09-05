package com.ishland.c2me.opts.natives_math.common.util;

public class MemoryUtil {

    public static int[] byte2int(byte[] data) {
        if (data == null) return null;
        int[] ints = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            ints[i] = data[i] & 0xff;
        }
        return ints;
    }

}
