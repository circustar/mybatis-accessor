package com.circustar.common_utils.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public abstract class AnnotationUtils {
    public static <T extends Annotation> List<T> getClassAnnotations(Class clazz, Class<T> annotationClass) {
        if(clazz.isAnnotationPresent(annotationClass)) {
            return (List<T>) Arrays.asList(clazz.getAnnotationsByType(annotationClass));
        }
        return Collections.emptyList();
    }

    public static <T extends Annotation> T getClassAnnotation(Class clazz, Class<T> annotationClass) {
        List<T> annotationList = getClassAnnotations(clazz, annotationClass);
        if (Objects.nonNull(annotationList) && !annotationList.isEmpty()) {
            return annotationList.get(0);
        }
        return null;
    }


    public static <T extends Annotation> T[] getFieldAnnotationsByName(Object obj, String name, Class<T> clazz) throws NoSuchFieldException {
        Field field = obj.getClass().getDeclaredField(name);
        return getFieldAnnotationsByName(field, clazz);
    }

    public static <T extends Annotation> T[] getFieldAnnotationsByName(Field field, Class<T> clazz) {
        return field.getAnnotationsByType(clazz);
    }

    public static <T extends Annotation> T getFieldAnnotation(Object obj, String name, Class<T> clazz) throws NoSuchFieldException {
        Field field = obj.getClass().getDeclaredField(name);
        return getFieldAnnotation(field, clazz);
    }

    public static <T extends Annotation> T getFieldAnnotation(Field field, Class<T> clazz) {
        return field.getAnnotation(clazz);
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
        Field field = clazz.getDeclaredField(fieldName);
        return field.getAnnotationsByType(annotationClass);
    }
}