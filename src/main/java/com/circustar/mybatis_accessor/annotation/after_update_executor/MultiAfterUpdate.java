package com.circustar.mybatis_accessor.annotation.after_update_executor;

import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface MultiAfterUpdate {
    AfterUpdate[] value();
}
