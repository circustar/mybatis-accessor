package com.circustar.mvcenhance.annotation;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Selector {
    String masterTableColumn() default "";
    Connector connector() default Connector.eq;
    String[] valueExpression() default "";
}
