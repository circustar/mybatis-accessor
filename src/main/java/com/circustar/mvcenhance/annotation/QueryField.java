package com.circustar.mvcenhance.annotation;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultiQueryField.class)
public @interface QueryField {
    String[] group() default "";
    String column() default "";
    Connector connector() default Connector.eq;
    int sortIndex() default Integer.MAX_VALUE;
    String sortOrder() default "asc";
    String expression() default "";
}
