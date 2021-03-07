package com.circustar.mybatis_accessor.utils;

import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class FieldUtils {
    public static String parseClassName(String name) {
        return Arrays.stream(name.trim().replace('-', '_').split("_"))
                .map(x -> x.substring(0,1).toUpperCase() + x.substring(1))
                .collect(Collectors.joining());
    }

    public static List<String> parsechildNames(String names) {
        return Arrays.stream(names.replace(" ", "").split(","))
                .map(x -> unCap(x)).collect(Collectors.toList());
    }

    public static String unCap(String name) {
        if(StringUtils.isEmpty(name)) {
            return name;
        }
        return name.substring(0,1).toLowerCase() + name.substring(1);

    }

    public static void setField(Object obj, Field field, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(obj, value);
    }

    public static void setFieldByName(Object obj, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    public static void setFieldByNameWithCollection(Object object, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        if(object == null) {return;}
        if(Collection.class.isAssignableFrom(object.getClass())) {
            Collection c = (Collection)object;
            for (Object obj : c) {
                setFieldByName(obj, name, value);
            }
        } else {
            setFieldByName(object, name, value);
        }
    }

    public static Object getValueByName(Object obj, String name) throws NoSuchFieldException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    public static Object getValue(Object obj, Field field) throws IllegalAccessException {
         field.setAccessible(true);
        return field.get(obj);
    }

    public static List<Field> getExistFields(Object obj, List<String> names, boolean throwable) throws NoSuchFieldException {
        List<Field> fields = new ArrayList<>();
        for(String name : names) {
            try {
                Field f = obj.getClass().getDeclaredField(name);
                fields.add(f);
            } catch (NoSuchFieldException ex) {
                if(throwable) {
                    throw ex;
                }
            }
        }
        return fields;
    }

}
