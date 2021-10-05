package com.circustar.mybatis_accessor.annotation.after_update;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface MultiAfterUpdate {
    AfterUpdate[] value();
}
