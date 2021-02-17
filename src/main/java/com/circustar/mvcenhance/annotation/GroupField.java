package com.circustar.mvcenhance.annotation;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GroupField {
    boolean groupBy() default true;
    String groupExpression() default "";
    int sortIndex() default Integer.MAX_VALUE;
    String sortOrder() default "asc";

}
