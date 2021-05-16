package org.yatopiamc.c2me.common.util;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

public class UnsafeUtils {
    public static ConcurrentHashMap<String, Class> reflectedClasses = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Constructor> reflectedConstructors = new ConcurrentHashMap<>();

    public static Constructor getReflectedConstructor(String className, Class<?>... parameterTypes) {
        try {
            Constructor returnConstructor = reflectedClasses.computeIfAbsent(className, name -> getReflectedClass(name)).getDeclaredConstructor(parameterTypes);
            assert returnConstructor != null;
            returnConstructor.setAccessible(true);
            return returnConstructor;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class getReflectedClass(String className) {
        try {
            System.out.println(className);
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
}
