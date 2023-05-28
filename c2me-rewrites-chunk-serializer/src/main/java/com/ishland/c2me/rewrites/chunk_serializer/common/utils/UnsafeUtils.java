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