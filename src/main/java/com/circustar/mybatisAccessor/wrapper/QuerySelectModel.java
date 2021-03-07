package com.circustar.mybatisAccessor.wrapper;

import com.circustar.mybatisAccessor.annotation.QueryGroupBy;
import com.circustar.mybatisAccessor.annotation.QuerySelect;
import org.springframework.util.StringUtils;

public class QuerySelectModel {
    private String expression;
    private String columnName;
    private String tableName;
    public QuerySelectModel(QueryGroupBy queryGroupBy, String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
        if(queryGroupBy != null && !StringUtils.isEmpty(queryGroupBy.expression())) {
            this.expression = queryGroupBy.expression() + " AS " + columnName;
        } else {
            this.expression = tableName + "." + columnName + " AS " + columnName;
        }
    }
    public QuerySelectModel(QuerySelect querySelect, String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
        if(querySelect != null && !StringUtils.isEmpty(querySelect.value())) {
            this.expression = querySelect.value() + " AS " + columnName;
        } else {
            this.expression = tableName + "." + columnName + " AS " + columnName;
        }
    }
    public QuerySelectModel(QuerySelect querySelect, String tableName, String columnName, String columnPrefix) {
        this.tableName = tableName;
        this.columnName = columnName;
        String prefix = tableName + "_";
        if(!StringUtils.isEmpty(columnPrefix)) {
            prefix = columnPrefix.endsWith("_") ? columnPrefix : (columnPrefix + "_");
        }
        if(querySelect != null && !StringUtils.isEmpty(querySelect.value())) {
            this.expression = querySelect.value() + " AS " + prefix + columnName;
        } else {
            this.expression = tableName + "." + columnName + " AS " + prefix + columnName;
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public String getExpression() {
        return expression;
    }
}
