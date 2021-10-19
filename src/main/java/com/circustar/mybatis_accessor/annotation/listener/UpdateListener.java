package com.circustar.mybatis_accessor.annotation.listener;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@Repeatable(MultiUpdateListener.class)
public @interface UpdateListener {
    String onExpression() default "";
    Class<? extends IUpdateEvent> afterUpdateExecutor();
    String[] updateParams() default "";
}
