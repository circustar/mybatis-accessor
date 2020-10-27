package org.yxy.circustar.mvc.enhance.utils;

import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FieldUtils {
    public static String parseClassNameFromPathVariable(String name) {
        return Arrays.stream(name.trim().replace('-', '_').split("_"))
                .map(x -> x.substring(0,1).toUpperCase() + x.substring(1))
                .collect(Collectors.joining());

    }

    public static String unCap(String name) {
        if(StringUtils.isEmpty(name)) {
            return name;
        }
        return name.substring(0,1).toLowerCase() + name.substring(1);

    }

    public static void setFieldByName(Object obj, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    public static Object getValueByName(Object obj, String name) throws NoSuchFieldException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    public static <T extends Annotation> T[] getFieldAnnotationByName(Object obj, String name, Class<T> clazz) throws NoSuchFieldException {
        Field f = obj.getClass().getDeclaredField(name);
        return f.getAnnotationsByType(clazz);
    }
}
