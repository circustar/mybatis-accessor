package com.circustar.mybatisAccessor.wrapper;

import com.circustar.mybatisAccessor.annotation.QueryGroupBy;
import org.springframework.util.StringUtils;

public class QueryGroupByModel {
    private String expression;

    public QueryGroupByModel(String expression) {
        this.expression = expression;
    }

    public QueryGroupByModel(QueryGroupBy queryGroupBy) {
        this(queryGroupBy.selectExpression());
    }

    public QueryGroupByModel(QueryGroupBy queryGroupBy, String tableName, String columnName) {
        if(StringUtils.isEmpty(queryGroupBy.expression())) {
            this.expression = tableName + "." + columnName;
        } else {
            this.expression = queryGroupBy.expression();
        }
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
