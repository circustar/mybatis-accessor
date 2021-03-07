package com.circustar.mybatis_accessor.annotation;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultiSelector.class)
public @interface Selector {
    String tableColumn() default "";
    Connector connector() default Connector.eq;
    String[] valueExpression() default "";
    int order() default 1;
}
