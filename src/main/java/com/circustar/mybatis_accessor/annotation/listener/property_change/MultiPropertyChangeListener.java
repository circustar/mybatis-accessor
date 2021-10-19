package com.circustar.mybatis_accessor.annotation.listener.property_change;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface MultiPropertyChangeListener {
    PropertyChangeListener[] value();
}
