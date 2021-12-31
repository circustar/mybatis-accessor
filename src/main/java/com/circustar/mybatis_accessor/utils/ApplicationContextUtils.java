package com.circustar.mybatis_accessor.utils;

import com.circustar.common_utils.reflection.ClassUtils;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Optional;

public abstract class ApplicationContextUtils {
    public static <T> T getBeanOrCreate(ApplicationContext applicationContext, Class<T> clazz) {
        final Collection<T> beanList = applicationContext.getBeansOfType(clazz).values();
        Optional<T> anyMatch = beanList.stream().filter(x -> x.getClass().equals(clazz)).findAny();
        if(!anyMatch.isPresent()) {
            anyMatch = beanList.stream().filter(x -> x.getClass().getName()
                    .startsWith(clazz.getName() + "$")).findAny();
        }
        if(anyMatch.isPresent()) {
            return anyMatch.get();
        }
        return ClassUtils.createInstance(clazz);
    }
}
