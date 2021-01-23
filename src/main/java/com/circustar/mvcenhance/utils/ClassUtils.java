package com.circustar.mvcenhance.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassUtils {
    public static List<Type[]> getTypeArguments(Class<?> clazz) {
        try {
            Type[] actualTypeArguments= clazz.getGenericInterfaces();
            if(actualTypeArguments != null && actualTypeArguments.length > 0) {
                return Arrays.stream(actualTypeArguments).map(x -> ((ParameterizedType)x).getActualTypeArguments()).collect(Collectors.toList());
            }
            return null;
        } catch (Exception ex) {
        }
        return null;
    }

    public static Type getFirstTypeArgument(Class<?> clazz) {
        try {
            Type[] actualTypeArguments= clazz.getGenericInterfaces();
            if(actualTypeArguments != null && actualTypeArguments.length > 0) {
                return ((ParameterizedType)actualTypeArguments[0]).getActualTypeArguments()[0];
            }
        } catch (Exception ex) {
        }
        return null;
    }
}
