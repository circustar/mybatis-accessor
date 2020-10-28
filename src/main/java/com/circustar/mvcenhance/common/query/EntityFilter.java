package com.circustar.mvcenhance.common.query;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultiEntityFilter.class)
public @interface EntityFilter {
    String[] group() default "";
    String column() default "";
    Connector connector() default Connector.eq;
    String[] valueExpression() default "";
}
