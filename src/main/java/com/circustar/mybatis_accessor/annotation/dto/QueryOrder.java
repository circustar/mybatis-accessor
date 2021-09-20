package com.circustar.mybatis_accessor.annotation.dto;

import java.lang.annotation.*;

@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface QueryOrder {
    String ORDER_DESC = "desc";
    String ORDER_ASC = "asc";

    String expression() default "";
    int sortIndex() default Integer.MAX_VALUE;
    String sortOrder() default ORDER_ASC;
}
