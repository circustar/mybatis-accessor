package com.circustar.mvcenhance.annotation;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GroupField {
    String selectExpression() default "";
    String groupByExpression() default "";
    String havingExpression() default "";
}
