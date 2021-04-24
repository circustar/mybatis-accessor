package com.circustar.common_utils.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class FieldUtils {
    public static String parseClassName(String name) {
        return Arrays.stream(name.trim().replace('-', '_').split("_"))
                .map(x -> x.substring(0,1).toUpperCase() + x.substring(1))
                .collect(Collectors.joining());
    }

    public static Object getFieldValue(Object obj, Method readMethod) throws InvocationTargetException, IllegalAccessException {
        if(obj != null && readMethod != null) {
            readMethod.setAccessible(true);
            return readMethod.invoke(obj);
        }
        return null;
    }

    public static void setFieldValue(Object obj, Method writeMethod, Object value) throws IllegalAccessException, InvocationTargetException {
        if(writeMethod == null || obj == null) {
            return;
        }
        writeMethod.setAccessible(true);
        writeMethod.invoke(obj, value);
    }
}
