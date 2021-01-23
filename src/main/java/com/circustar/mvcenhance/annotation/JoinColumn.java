package com.circustar.mvcenhance.annotation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface JoinColumn {
    String masterTableColumn() default "";
    Connector connector() default Connector.eq;
    String[] value() default "";
}
