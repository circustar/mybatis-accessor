package com.circustar.mvcenhance.annotation;

public class GroupFieldModel {
    private String selectExpression;
    private String groupByExpression;
    private String havingExpression;

    public GroupFieldModel(String selectExpression, String groupByExpression, String havingExpression) {
        this.selectExpression = selectExpression;
        this.groupByExpression = groupByExpression;
        this.havingExpression = havingExpression;
    }

    public GroupFieldModel(GroupField groupField) {
        this(groupField.selectExpression(), groupField.groupByExpression(), groupField.havingExpression());
    }

    public String getSelectExpression() {
        return selectExpression;
    }

    public String getGroupByExpression() {
        return groupByExpression;
    }

    public String getHavingExpression() {
        return havingExpression;
    }
}
