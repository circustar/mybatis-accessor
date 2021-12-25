package com.circustar.mybatis_accessor.utils;

import com.circustar.common_utils.reflection.ClassUtils;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Optional;

public class ApplicationContextUtils {
    public static <T> T getBeanOrCreate(ApplicationContext applicationContext, Class<T> clazz) {
        try {
            // TODO: 增强
            final Map<String, T> beansOfType = applicationContext.getBeansOfType(clazz);
            final Optional<T> anyMatch = beansOfType.values().stream().filter(x -> x.getClass().equals(clazz)).findAny();
            if(anyMatch.isPresent()) {
                return anyMatch.get();
            }
            return ClassUtils.createInstance(clazz);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
