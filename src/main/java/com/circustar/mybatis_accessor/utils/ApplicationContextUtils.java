package com.circustar.mybatis_accessor.utils;

import com.circustar.common_utils.reflection.ClassUtils;
import org.springframework.context.ApplicationContext;

public class ApplicationContextUtils {
    public static <T> T getBeanOrCreate(ApplicationContext applicationContext, Class<T> clazz) {
        T bean;
        try {
            String[] beanNamesForType = applicationContext.getBeanNamesForType(clazz);
            if(beanNamesForType!= null && beanNamesForType.length > 0) {
                bean = applicationContext.getBean(beanNamesForType[0], clazz);
            } else {
                bean = ClassUtils.createInstance(clazz);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return bean;
    }
}
