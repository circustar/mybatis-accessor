package com.circustar.mvcenhance.common.query;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultiSelector.class)
public @interface Selector {
    String masterTableColumn() default "";
    Connector connector() default Connector.eq;
    String[] valueExpression() default "";
    //String directSql() default "";
}
