package com.circustar.mybatis_accessor.annotation.dto.calc;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface NumberAssigner {
    String summaryFieldName();
    String partitionFieldName();
    String accordingToFieldName();
    int precision() default 0;
}
