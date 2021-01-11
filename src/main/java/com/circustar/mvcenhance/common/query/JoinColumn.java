package com.circustar.mvcenhance.common.query;

import com.circustar.mvcenhance.common.query.Connector;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface JoinColumn {
    //String alias();
    String masterTableColumn() default "";
    Connector connector() default Connector.eq;
    String[] value() default "";
    //String directSql() default "";
}
