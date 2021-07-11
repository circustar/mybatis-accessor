package com.circustar.mybatis_accessor.annotation;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryGroupBy {
    String expression() default "";
}
