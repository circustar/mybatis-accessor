package com.circustar.mvcenhance.wrapper;

import com.circustar.mvcenhance.annotation.OrderField;

public class OrderFieldModel {
    private String orderExpression;
    private int sortIndex;
    private String sortOrder;

    public OrderFieldModel(String orderExpression, int sortIndex, String sortOrder) {
        this.orderExpression = orderExpression;
        this.sortIndex = sortIndex;
        this.sortOrder = sortOrder;
    }
    public OrderFieldModel(OrderField queryField) {
        this(queryField.orderExpression(), queryField.sortIndex(), queryField.sortOrder());
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
