package com.circustar.mybatis_accessor.annotation.event;

import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@Repeatable(MultiDecodeEvent.class)
public @interface DecodeEvent {
    String onExpression();
    String[] targetProperties();
    String[] matchProperties();
    Class sourceDtoClass();
    String[] sourceProperties() default "";
    String[] matchSourceProperties() default {};
    boolean errorWhenNotExist() default true;
    IUpdateCommand.UpdateType[] updateType() default {};
    ExecuteTiming executeTiming() default ExecuteTiming.DEFAULT;
}
