package com.circustar.common_utils.reflection;

import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public abstract class FieldUtils {
    public static String parseClassName(String name) {
        return Arrays.stream(name.trim().replace('-', '_').split("_"))
                .map(x -> x.substring(0,1).toUpperCase(Locale.getDefault()) + x.substring(1))
                .collect(Collectors.joining());
    }

    public static Object getFieldValue(Object obj, Method readMethod) {
        try {
            if (obj != null && readMethod != null) {
                readMethod.setAccessible(true);
                return readMethod.invoke(obj);
            }
        } catch (Exception ex) {
            try {
                final Method declaredMethod = obj.getClass().getDeclaredMethod(readMethod.getName());
                declaredMethod.setAccessible(true);
                return declaredMethod.invoke(obj);
            } catch (Exception ex2) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }

    public static void setFieldValue(Object obj, Method writeMethod, Object value) {
        if(writeMethod == null || obj == null) {
            return;
        }
        try {
            writeMethod.setAccessible(true);
            writeMethod.invoke(obj, value);
        } catch (Exception ex) {
            try {
                final Method declaredMethod = obj.getClass().getDeclaredMethod(writeMethod.getName(), value.getClass());
                declaredMethod.setAccessible(true);
                declaredMethod.invoke(obj, value);
            } catch (Exception ex2) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static void setFieldValueIfNull(Object obj, Method readMethod, Method writeMethod, Object value) {
        if(writeMethod == null || obj == null || readMethod == null) {
            return;
        }
        try {
            readMethod.setAccessible(true);
            Object property = readMethod.invoke(obj);
            if(property != null) {
                return;
            }

            writeMethod.setAccessible(true);
            writeMethod.invoke(obj, value);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Field getField(Class clazz, String name) {
        if(clazz == Object.class || clazz.isPrimitive()) {
            return null;
        }
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            return getField(clazz.getSuperclass(), name);
        }
    }

    public static List<PropertyDescriptor> getPropertyDescriptors(Class clazz) {
        List<PropertyDescriptor> result = new ArrayList<>();
        if(clazz == Object.class || clazz.isPrimitive()) {
            return result;
        }
        result.addAll(Arrays.asList(BeanUtils.getPropertyDescriptors(clazz)).stream()
                .filter(x -> x.getPropertyType() != Class.class).collect(Collectors.toList()));
        return result;
    }
}
