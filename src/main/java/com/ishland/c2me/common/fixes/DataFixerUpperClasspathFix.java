package com.ishland.c2me.common.fixes;

import net.fabricmc.loader.util.UrlUtil;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;

public class DataFixerUpperClasspathFix {

    private static final String NAME = "com/mojang/datafixers/util/Either.class";

    public static void fix() {
        try {
            // extra checks for dev env
            final ClassLoader classLoader = DataFixerUpperClasspathFix.class.getClassLoader();
            final Class<?> classLoaderInterface = Class.forName("net.fabricmc.loader.launch.knot.KnotClassLoaderInterface");
            if (classLoaderInterface.isInstance(classLoader)) {
                final InputStream stream = (InputStream) accessible(classLoaderInterface.getMethod("getResourceAsStream", String.class, boolean.class)).invoke(classLoader, NAME, true);
                if (stream != null) {
                    stream.close();
                    return;
                }

                final URL resource = classLoader.getResource(NAME);
                assert resource != null;
                final String resourcePath = resource.getPath();
                final URL url = UrlUtil.asUrl(Path.of("/", resourcePath.substring(resourcePath.indexOf('/'), resourcePath.lastIndexOf('!'))).toAbsolutePath());
                System.out.println(String.format("Purposed %s to classpath", url));
                accessible(classLoaderInterface.getMethod("addURL", URL.class)).invoke(classLoader, url);
            }
        } catch (Throwable t) {
            System.err.println("Something went wrong with DataFixerUpperClasspathFix");
            t.printStackTrace();
        }
    }

    private static Field accessible(Field field) {
        field.setAccessible(true);
        return field;
    }

    private static Method accessible(Method method) {
        method.setAccessible(true);
        return method;
    }

}
