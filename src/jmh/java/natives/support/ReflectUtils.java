package natives.support;

import java.lang.reflect.Field;

public class ReflectUtils {

    public static Object getField(Class<?> clazz, Object instance, String fieldName) {
        try {
            final Field declaredField = clazz.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField.get(instance);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
