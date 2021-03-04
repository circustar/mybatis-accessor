package com.circustar.mvcenhance.wrapper;

import com.circustar.mvcenhance.annotation.QueryHaving;

public class QueryHavingModel {
    public QueryHavingModel(String expression) {
        this.expression = expression;
    }
    public QueryHavingModel(QueryHaving queryHaving) {
        this(queryHaving.expression());
    }

    private String expression;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
