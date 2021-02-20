package com.circustar.mvcenhance.annotation;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultiSelector.class)
public @interface Selector {
    String tableColumn() default "";
    Connector connector() default Connector.eq;
    String[] valueExpression() default "";
}
