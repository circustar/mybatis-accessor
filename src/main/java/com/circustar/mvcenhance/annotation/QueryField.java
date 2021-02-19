package com.circustar.mvcenhance.annotation;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
//@Repeatable(MultiQueryField.class)
public @interface QueryField {
    String queryExpression() default "";
    Connector connector() default Connector.eq;
}
