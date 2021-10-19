package com.circustar.mybatis_accessor.annotation.listener;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface MultiUpdateListener {
    UpdateListener[] value();
}
