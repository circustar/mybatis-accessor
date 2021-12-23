package com.circustar.common_utils.reflection;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    public static <T> T createInstance(Class<T> clazz) {
        try {
            Constructor<?>[] constructors = clazz.getConstructors();
            Optional<Constructor<?>> any = Arrays.stream(constructors).filter(x -> x.getParameterCount() == 0).findAny();
            T t;
            if(any.isPresent()) {
                t = clazz.newInstance();
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
                t = (T) constructor.newInstance(param);
            }
            return t;
        } catch (Exception ex) {
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
