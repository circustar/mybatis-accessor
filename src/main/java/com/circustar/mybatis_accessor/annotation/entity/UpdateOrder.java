package com.circustar.mybatis_accessor.annotation.entity;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface UpdateOrder {
    int value() default Integer.MAX_VALUE;
}
