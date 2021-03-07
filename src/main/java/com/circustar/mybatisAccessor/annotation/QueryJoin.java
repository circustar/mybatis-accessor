package com.circustar.mybatisAccessor.annotation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface QueryJoin {
    String joinString() default "";
    int order() default 1;
    JoinType joinType() default JoinType.LEFT;

    enum JoinType {
        LEFT("left join"), RIGHT("right join"), INNER("inner join"), FULL("full join");
        private String joinString;
        JoinType(String joinString) {
            this.joinString = joinString;
        }
        public String getJoinString() {
            return joinString;
        }
    }
}
