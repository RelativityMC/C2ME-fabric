package com.ishland.c2me.rewrites.chunk_serializer.common.utils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtils {
    public static ConcurrentHashMap<String, Class> reflectedClasses = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<ClassField, Field> reflectedFields = new ConcurrentHashMap<>();

    public static Class getClass(String className) {
        return reflectedClasses.computeIfAbsent(className, ReflectionUtils::getReflectedClass);
    }

    public static Field getField(Class clss, String fild) {
        return reflectedFields.computeIfAbsent(new ClassField(clss, fild), ReflectionUtils::getReflectedField);
    }

    public static void setFieldToPublic(Field fild) {
        setAccessibleObjectToPublic(fild);
    }

    public static void setMethodToPublic(Method mthod) {
        setAccessibleObjectToPublic(mthod);
    }

    private static void setAccessibleObjectToPublic(AccessibleObject obj) {
        try {
            obj.setAccessible(true);
        } catch (SecurityException se) {
            try {
                Class.forName("java.security.AccessController");
                AccessControllerUtils.runWithAccessController(() -> {
                    obj.setAccessible(true);
                    return null;
                });
            } catch (ClassNotFoundException | IllegalAccessException e) {
                if (UnsafeUtils.canUseUnsafe) {
                    UnsafeUtils.putFieldBoolean(obj, getField(obj.getClass(), "override"), true);
                }
            }
        }
    }

    private static Field getReflectedField(ClassField clssfild) {
        try {
            return clssfild.clss.getField(clssfild.fild());
        } catch (NoSuchFieldException ignored) {

        }
        return null;
    }

    private static Class getReflectedClass(String className) {
        try {
            Class classObj;
            int $loc = className.indexOf("$");
            if ($loc > -1) {
                classObj = getNestedClass(Class.forName(className.substring(0, $loc)), className.substring($loc + 1));
            } else {
                classObj = Class.forName(className);
            }
            assert classObj != null;
            return classObj;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Class getNestedClass(Class upperClass, String nestedClassName) {
        Class[] classObjArr = upperClass.getDeclaredClasses();
        for (Class classArrObj : classObjArr) {
            if (classArrObj.getName().equals(upperClass.getName() + "$" + nestedClassName)) {
                return classArrObj;
            }
        }
        return null;
    }

    private record ClassField(Class clss, String fild) {};
}
