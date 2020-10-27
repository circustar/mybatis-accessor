package org.yxy.circustar.mvc.common.query;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultiJoin.class)
public @interface Join {
    String[] group() default "";
    String column() default "";
    Connector connector() default Connector.eq;
    String[] valueExpression() default "";
}
