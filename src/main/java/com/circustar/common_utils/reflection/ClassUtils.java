package com.circustar.common_utils.reflection;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ClassUtils {
    public static List<Type[]> getTypeArguments(Class<?> clazz) {
        Type[] actualTypeArguments= clazz.getGenericInterfaces();
        if(actualTypeArguments != null && actualTypeArguments.length > 0) {
            return Arrays.stream(actualTypeArguments).map(x -> ((ParameterizedType)x).getActualTypeArguments()).collect(Collectors.toList());
        }
        return null;
    }

    public static Type getFirstTypeArgument(Class<?> clazz) {
        Type[] actualTypeArguments= clazz.getGenericInterfaces();
        if(actualTypeArguments != null && actualTypeArguments.length > 0) {
            return ((ParameterizedType)actualTypeArguments[0]).getActualTypeArguments()[0];
        }
        return null;
    }

    public static <T> T createInstance(Class<T> clazz) {
        try {
            Constructor<?>[] constructors = clazz.getConstructors();
            Optional<Constructor<?>> any = Arrays.stream(constructors).filter(x -> x.getParameterCount() == 0).findAny();
            T target;
            if(any.isPresent()) {
                target = clazz.newInstance();
            } else {
                Constructor<?> constructor = constructors[0];
                Parameter[] parameters = constructor.getParameters();
                int parameterCount = parameters.length;
                Object[] param = new Object[parameterCount];
                for(int i = 0; i < parameterCount; i++) {
                    if(parameters[i].getType().isPrimitive()) {
                        param[i] = getPrimitiveDefaultValue(parameters[i].getType());
                    }
                }
                target = (T) constructor.newInstance(param);
            }
            return target;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Object getPrimitiveDefaultValue(Class primitiveType) {
        if(int.class == primitiveType) {
            return 0;
        } else if(long.class == primitiveType) {
            return 0L;
        } else if(boolean.class == primitiveType) {
            return false;
        } else if(double.class == primitiveType) {
            return 0d;
        } else if(short.class == primitiveType) {
            return (short)0;
        } else if(byte.class == primitiveType) {
            return (byte)0;
        } else if(float.class == primitiveType) {
            return 0f;
        } else if(char.class == primitiveType) {
            return ' ';
        }
        return null;
    }
}
