package com.circustar.mybatis_accessor.annotation.event;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface MultiUpdateEvent {
    UpdateEvent[] value();
}
