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

package com.ishland.c2me.rewrites.chunk_serializer.common.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeUtils {
    public static final Unsafe UNSAFE = findUnsafe();

    public static boolean canUseUnsafe = true;

    private static Unsafe findUnsafe() {
        try {
            return Unsafe.getUnsafe();
        } catch (SecurityException se) {
            try {
                try {
                    Class.forName("java.security.AccessController");
                    return AccessControllerUtils.runWithAccessController(() -> {
                        try {
                            return UnsafeUtils.getUnsafeWithoutAccessController();
                        } catch (IllegalAccessException e) {
                            System.out.println("Unsafe Unavailable" + System.lineSeparator() + e);
                            canUseUnsafe = false;
                            return null;
                        }
                    });
                } catch (ClassNotFoundException e) {
                    if (canUseUnsafe) {
                        return getUnsafeWithoutAccessController();
                    }
                }
            } catch (Exception e) {
                System.out.println("Unsafe Unavailable" + System.lineSeparator() + e);
                canUseUnsafe = false;
                return null;
            }
        }
        return null;
    }

    static Unsafe getUnsafeWithoutAccessController() throws IllegalAccessException {
        Class<Unsafe> type = Unsafe.class;
        try {
            Field field = ReflectionUtils.getField(type, "theUnsafe");
            if (field == null) throw new RuntimeException("Field not found");
            ReflectionUtils.setFieldToPublic(field);
            return type.cast(field.get(type));
        } catch (Exception e) {
            for (Field field : type.getDeclaredFields()) {
                if (type.isAssignableFrom(field.getType())) {
                    ReflectionUtils.setFieldToPublic(field);
                    return type.cast(field.get(type));
                }
            }
        }
        return null;
    }


    public static <T> Object getStaticFieldObject(Class<T> targetClass, Field field) {
        assert UNSAFE != null;
        return UNSAFE.getObject(UNSAFE.staticFieldBase(field), UNSAFE.staticFieldOffset(field));
    }

    public static Object getFieldInt(Object targetObject, Field field) {
        assert UNSAFE != null;
        return UNSAFE.getInt(targetObject, UNSAFE.objectFieldOffset(field));
    }

    public static long getFieldLong(Object targetObject, Field field) {
        assert UNSAFE != null;
        return UNSAFE.getLong(targetObject, UNSAFE.objectFieldOffset(field));
    }

    public static Object getFieldObject(Object targetObject, Field field) {
        assert UNSAFE != null;
        return UNSAFE.getObject(targetObject, UNSAFE.objectFieldOffset(field));
    }

    public static void putFieldObject(Object targetObject, Field field, Object in) {
        assert UNSAFE != null;
        UNSAFE.putObject(targetObject, UNSAFE.objectFieldOffset(field), in);
    }

    public static void putFieldBoolean(Object targetObject, Field field, Boolean in) {
        assert UNSAFE != null;
        UNSAFE.putBoolean(targetObject, UNSAFE.objectFieldOffset(field), in);
    }
}