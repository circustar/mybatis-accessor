package com.circustar.mybatis_accessor.annotation.entity;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IdReference {
}
