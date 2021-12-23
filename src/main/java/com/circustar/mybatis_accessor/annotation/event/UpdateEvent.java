package com.circustar.mybatis_accessor.annotation.event;

import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@Repeatable(MultiUpdateEvent.class)
public @interface UpdateEvent {
    String onExpression() default "";
    Class<? extends IUpdateEvent> updateEventClass();
    String[] updateParams() default "";
    IUpdateCommand.UpdateType[] updateType() default {IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE};
    ExecuteTiming executeTiming() default ExecuteTiming.NONE;
}
