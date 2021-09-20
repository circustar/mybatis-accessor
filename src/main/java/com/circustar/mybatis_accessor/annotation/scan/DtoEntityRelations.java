package com.circustar.mybatis_accessor.annotation.scan;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DtoEntityRelations {
    DtoEntityRelation[] value();
}
