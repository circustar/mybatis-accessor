package com.circustar.mvcenhance.annotation;

class QueryFieldModel {
    public QueryFieldModel(String queryExpression, Connector connector) {
        this.queryExpression = queryExpression;
        this.connector = connector;
    }
    public QueryFieldModel(String queryExpression) {
        this(queryExpression, Connector.eq);
    }
    public QueryFieldModel(QueryField queryField) {
        this(queryField.queryExpression(), queryField.connector());
    }

    String queryExpression;
    Connector connector;

    public String getQueryExpression() {
        return queryExpression;
    }

    public void setQueryExpression(String queryExpression) {
        this.queryExpression = queryExpression;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

}
