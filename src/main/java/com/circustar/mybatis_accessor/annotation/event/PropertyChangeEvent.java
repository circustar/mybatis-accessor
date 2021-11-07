package com.circustar.mybatis_accessor.annotation.event;

import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.listener.event.IPropertyChangeEvent;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@Repeatable(MultiPropertyChangeEvent.class)
public @interface PropertyChangeEvent {
    String[] changeProperties();
    boolean triggerOnAnyChanged() default true;
    Class<? extends IPropertyChangeEvent> onChangeExecutor();
    String[] updateParams() default "";
    IUpdateCommand.UpdateType[] updateType() default {};
    ExecuteTiming executeTiming() default ExecuteTiming.NONE;
}
