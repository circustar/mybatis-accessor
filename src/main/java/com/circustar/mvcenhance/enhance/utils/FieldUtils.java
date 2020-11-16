package com.circustar.mvcenhance.enhance.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class FieldUtils {
    public static String parseClassName(String name) {
        return Arrays.stream(name.trim().replace('-', '_').split("_"))
                .map(x -> x.substring(0,1).toUpperCase() + x.substring(1))
                .collect(Collectors.joining());
    }

    public static List<String> parseSubEntityNames(String names) {
        return Arrays.stream(names.replace(" ", "").split(","))
                .map(x -> unCap(x)).collect(Collectors.toList());
    }

    public static String unCap(String name) {
        if(StringUtils.isEmpty(name)) {
            return name;
        }
        return name.substring(0,1).toLowerCase() + name.substring(1);

    }

    public static void setFieldByName(Object obj, String name, Object value) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    public static void setFieldByNameWithCollection(Object object, String name, Object value) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
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

//    public static void setFieldByName(Object obj, String name, Object value, EnhancedConversionService conversionService) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
//        Field f = obj.getClass().getDeclaredField(name);
//        f.setAccessible(true);
//        Object finalValue = value;
//        if(finalValue != null) {
//            if(f.getType() != finalValue.getClass()) {
//                finalValue = conversionService.convert(finalValue, f.getType());
//            }
//        }
//        f.set(obj, finalValue);
//    }

    public static Object getValueByName(Object obj, String name) throws NoSuchFieldException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    public static <T extends Annotation> T[] getFieldAnnotationByName(Object obj, String name, Class<T> clazz) throws NoSuchFieldException {
        Field f = obj.getClass().getDeclaredField(name);
        return f.getAnnotationsByType(clazz);
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
    public static <T extends Annotation> void parseFieldAnnotationToMap(List<Field> fields
            , Class<T> clazz, Map<String, T[]> existMap, List<String> noAnnotationInfoList) {
        for(Field f : fields) {
            T[] annotations = f.getAnnotationsByType(clazz);
            if(annotations != null && annotations.length > 0) {
                existMap.put(f.getName(), annotations);
            } else {
                noAnnotationInfoList.add(f.getName());
            }
        }
    }

    public static <T extends Annotation> T[] getFieldAnnotationsByType(Class clazz, String fieldName
            , Class<T> annotationClass) throws NoSuchFieldException {
        Field f = clazz.getDeclaredField(fieldName);
        return f.getAnnotationsByType(annotationClass);
    }

    public static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
    }
}
