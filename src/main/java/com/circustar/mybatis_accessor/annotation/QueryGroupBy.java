package com.circustar.mybatis_accessor.annotation;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryGroupBy {
    String selectExpression() default "";
    String expression() default "";
    String havingExpression() default "";
}
