package com.circustar.mybatis_accessor.converter;

public interface IConverter<S, T> {
    T convert(Class<T> clazz, S s) ;
}
