package com.circustar.mybatis_accessor.annotation.listener.property_change;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@Repeatable(MultiPropertyChangeListener.class)
public @interface PropertyChangeListener {
    String[] changeProperties();
    boolean triggerOnAnyChanged() default true;
    Class<? extends IPropertyChangeEvent> onChangeExecutor();
    String[] updateParams() default "";
}
