package com.circustar.mvcenhance.common.query;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultiQueryField.class)
public @interface QueryField {
    String[] group() default "";
    String column() default "";
    Connector connector() default Connector.eq;
    int sortIndex() default 0;
    String sortOrder() default "";
    String expression() default "";
    boolean ignoreEmpty() default true;
}
