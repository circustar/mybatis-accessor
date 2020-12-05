package com.circustar.mvcenhance.enhance.utils;

import com.circustar.mvcenhance.enhance.update.DeleteField;

import java.lang.annotation.Annotation;
import java.util.*;

public class AnnotationUtils {
    public static <T extends Annotation> T[] getClassAnnotations(Class clazz, Class<T> annotationClass) {
        if(clazz.isAnnotationPresent(annotationClass)) {
            return (T[]) clazz.getAnnotationsByType(annotationClass);
        }
        return null;
    }

    public static <T extends Annotation> T getClassAnnotation(Class clazz, Class<T> annotationClass) {
        T[] annotationList = getClassAnnotations(clazz, annotationClass);
        if (Objects.nonNull(annotationList) && annotationList.length > 0) {
            return annotationList[0];
        }
        return null;
    }

    public static String getDeleteFieldAnnotationValue(Class clazz) {
        DeleteField df = getClassAnnotation(clazz, DeleteField.class);
        if(df != null) {
            return df.value();
        }
        return null;
    }

//    public static TableJoiner[] getTableJoinerFields(Class clazz) {
//        TableJoiner df = getClassAnnotations(clazz, TableJoiner.class);
//    }
}