package com.circustar.mybatis_accessor.annotation.after_update;

import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@Repeatable(MultiAfterUpdate.class)
public @interface AfterUpdate {
    String onExpression() default "";
    Class<? extends IAfterUpdateExecutor> afterUpdateExecutor();
    String[] updateParams();
    IUpdateCommand.UpdateType[] updateTypes() default {IUpdateCommand.UpdateType.INSERT
            , IUpdateCommand.UpdateType.UPDATE};
}
