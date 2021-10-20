package com.circustar.mybatis_accessor.converter;

import org.springframework.beans.BeanUtils;

public class DefaultConverter<T, R> implements IConverter<T, R> {
    public R convert(Class<R> clazz, T t) {
        try {
            R r = clazz.newInstance();
            BeanUtils.copyProperties(t, r);
            return r;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
