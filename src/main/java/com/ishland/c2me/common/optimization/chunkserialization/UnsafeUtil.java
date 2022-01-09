package com.ishland.c2me.common.optimization.chunkserialization;

import com.ishland.c2me.C2MEMod;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeUtil {
    private static final Unsafe INSTANCE;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);

            INSTANCE = (Unsafe) field.get(null);
        } catch (ReflectiveOperationException e) {
            C2MEMod.LOGGER.error("Could not initialize unsafe utilities", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Unsafe getInstance() {
        return INSTANCE;
    }
}