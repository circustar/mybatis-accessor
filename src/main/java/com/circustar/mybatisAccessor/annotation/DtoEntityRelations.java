package com.circustar.mybatisAccessor.annotation;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DtoEntityRelations {
    DtoEntityRelation[] value();
}
