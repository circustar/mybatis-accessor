package com.circustar.mybatis_accessor.annotation.on_change;

import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@Repeatable(MultiOnChange.class)
public @interface OnChange {
    String[] changeProperties();
    boolean triggerOnAnyChanged() default true;
    Class<? extends IOnChangeExecutor> onChangeExecutor();
    String[] updateParams() default "";
}
