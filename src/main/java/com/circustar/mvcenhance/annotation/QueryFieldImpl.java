package com.circustar.mvcenhance.annotation;

class QueryFieldImpl {
    public QueryFieldImpl() {
    }
    public QueryFieldImpl(String column, Object value) {
        this.group = "";
        this.column = column;
        this.connector = Connector.eq;
        this.sortIndex = Integer.MAX_VALUE;
        this.value = value;
        this.sortOrder = QueryFieldModel.ORDER_ASC;
        this.expression = "";
    }
    String group;
    String column;
    Connector connector;
    int sortIndex;
    String sortOrder;
    Object value;
    String expression;
    boolean ignoreEmpty;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}