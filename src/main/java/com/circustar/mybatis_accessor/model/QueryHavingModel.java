package com.circustar.mybatis_accessor.model;

import com.circustar.mybatis_accessor.annotation.dto.QueryHaving;

public class QueryHavingModel {
    private String expression;

    public QueryHavingModel(String expression) {
        this.expression = expression;
    }
    public QueryHavingModel(QueryHaving queryHaving) {
        this(queryHaving.expression());
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
