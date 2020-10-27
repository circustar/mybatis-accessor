package org.yxy.circustar.mvc.enhance.relation;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DtpEntityRelations {
    DtoEntityRelation[] value();
}
