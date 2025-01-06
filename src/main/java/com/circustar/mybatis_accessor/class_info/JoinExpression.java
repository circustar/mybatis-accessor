package com.circustar.mybatis_accessor.class_info;

import java.util.List;

public class JoinExpression {

    public JoinExpression(String joinName){
        this.joinName = joinName;
    }

    public JoinExpression(String joinName, List<String> columnNames, String joinString) {
        this(joinName);
        this.columnNames = columnNames;
        this.joinString = joinString;
    }

    private String joinName;

    private List<String> columnNames;
    private String joinString;

    public String getJoinName() {
        return joinName;
    }

    public void setJoinName(String joinName) {
        this.joinName = joinName;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public String getJoinString() {
        return joinString;
    }

    public void setJoinString(String joinString) {
        this.joinString = joinString;
    }
}
