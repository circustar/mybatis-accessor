package com.circustar.mybatis_accessor.annotation.dto;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface QueryJoin {
    String joinExpression() default "";
    int order() default 1;
    JoinType joinType() default JoinType.LEFT;
    String tableAlias() default "";
    String subQueryExpression() default "";

    enum JoinType {
        LEFT("left join"), RIGHT("right join"), INNER("inner join"), FULL("full join");
        private String joinExpression;

        JoinType(String joinExpression) {
            this.joinExpression = joinExpression;
        }
        public String getJoinExpression() {
            return joinExpression;
        }

    }
}
