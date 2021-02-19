package com.circustar.mvcenhance.wrapper;

import com.circustar.mvcenhance.annotation.GroupField;

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

    public void setSelectExpression(String selectExpression) {
        this.selectExpression = selectExpression;
    }

    public void setGroupByExpression(String groupByExpression) {
        this.groupByExpression = groupByExpression;
    }

    public void setHavingExpression(String havingExpression) {
        this.havingExpression = havingExpression;
    }
}
