package com.circustar.mybatis_accessor.converter;

import com.circustar.common_utils.reflection.ClassUtils;
import org.springframework.beans.BeanUtils;

public class DefaultConverter<S, T> implements IConverter<S, T> {
    public T convert(Class<T> clazz, S s) {
        T t = ClassUtils.createInstance(clazz);
        BeanUtils.copyProperties(s, t);
        return t;
    }
}
