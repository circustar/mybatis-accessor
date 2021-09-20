package com.circustar.mybatis_accessor.annotation.dto;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface QueryHaving {
    String expression() default "";
}
