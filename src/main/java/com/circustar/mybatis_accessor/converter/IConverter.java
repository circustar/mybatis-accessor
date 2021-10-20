package com.circustar.mybatis_accessor.converter;

public interface IConverter<T, R> {
    public R convert(Class<R> clazz, T t) ;
}
