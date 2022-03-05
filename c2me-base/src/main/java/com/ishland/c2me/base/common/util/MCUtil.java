package com.ishland.c2me.base.common.util;

import net.minecraft.util.math.ChunkPos;

public class MCUtil {

    private MCUtil() {
    }

    public static long toLong(ChunkPos pos) {
        return ((long)pos.x) | ((long)pos.z) << 32;
    }

}
