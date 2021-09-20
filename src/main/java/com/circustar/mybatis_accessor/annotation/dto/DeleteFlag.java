package com.circustar.mybatis_accessor.annotation.dto;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface DeleteFlag {
    boolean physicDelete() default false;
}
