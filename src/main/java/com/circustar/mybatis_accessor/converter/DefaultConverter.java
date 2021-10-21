package com.circustar.mybatis_accessor.converter;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

public class DefaultConverter<S, T> implements IConverter<S, T> {
    public T convert(Class<T> clazz, S s) {
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
            BeanUtils.copyProperties(s, t);
            return t;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    protected Object getPrimitiveDefaultValue(Class primitiveType) {
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
