package com.circustar.mvcenhance.relation;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DtoEntityRelations {
    DtoEntityRelation[] value();
}
