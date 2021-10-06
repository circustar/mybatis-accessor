package com.circustar.mybatis_accessor.annotation.dto;

import com.circustar.mybatis_accessor.annotation.Connector;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultiSelector.class)
public @interface Selector {
    String tableColumn() default "";
    Connector connector() default Connector.eq;
    String[] valueExpression();
    int order() default 1;
}
