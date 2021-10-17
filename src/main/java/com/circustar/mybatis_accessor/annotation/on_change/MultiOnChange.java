package com.circustar.mybatis_accessor.annotation.on_change;

import com.circustar.mybatis_accessor.annotation.after_update.AfterUpdate;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface MultiOnChange {
    OnChange[] value();
}
