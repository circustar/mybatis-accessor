package com.circustar.mvcenhance.common.query;

import com.circustar.mvcenhance.common.query.Connector;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface JoinColumn {
    //String alias();
    String masterColumn() default "";
    JoinConnector connector() default JoinConnector.eq;
    String[] values();
    String directSql() default "";
}
