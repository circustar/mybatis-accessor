package com.circustar.mybatis_accessor.converter;

import com.circustar.common_utils.reflection.ClassUtils;
import org.springframework.beans.BeanUtils;

public class DefaultConverter<S, T> implements IConverter<S, T> {
    @Override
    public T convert(Class<T> clazz, S source) {
        T target = ClassUtils.createInstance(clazz);
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
