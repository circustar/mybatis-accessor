package com.circustar.mvcenhance.annotation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface JoinTable {
    String alias();
    JoinColumn[] joinColumns();
    int order() default 1;
}
