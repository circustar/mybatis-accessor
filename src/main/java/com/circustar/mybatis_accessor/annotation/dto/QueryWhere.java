package com.circustar.mybatis_accessor.annotation.dto;

import com.circustar.mybatis_accessor.annotation.Connector;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface QueryWhere {
    String tableColumn() default "";
    Connector connector() default Connector.eq;
    String expression() default "";
}
