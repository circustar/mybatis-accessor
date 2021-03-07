package com.circustar.mybatis_accessor.wrapper;

import com.circustar.mybatis_accessor.annotation.QueryOrder;

public class QueryOrderModel {
    private String orderExpression;
    private int sortIndex;
    private String sortOrder;

    public QueryOrderModel(String orderExpression, int sortIndex, String sortOrder) {
        this.orderExpression = orderExpression;
        this.sortIndex = sortIndex;
        this.sortOrder = sortOrder;
    }
    public QueryOrderModel(QueryOrder queryField) {
        this(queryField.expression(), queryField.sortIndex(), queryField.sortOrder());
    }

    public String getOrderExpression() {
        return orderExpression;
    }

    public void setOrderExpression(String orderExpression) {
        this.orderExpression = orderExpression;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public String getSortOrder() {
        return sortOrder;
    }
}
