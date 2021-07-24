package com.circustar.mybatis_accessor.model;

import com.circustar.mybatis_accessor.annotation.QueryJoin;
import org.springframework.util.StringUtils;

public class QueryJoinModel {
    private String joinExpression;
    private int order;
    private QueryJoin.JoinType joinType;
    private String tableAlias;

    public QueryJoinModel(String tableAlias, String joinExpression, int order, QueryJoin.JoinType joinType) {
        this.tableAlias = tableAlias;
        this.joinExpression = joinExpression;
        this.order = order;
        this.joinType = joinType;
    }
    public QueryJoinModel(QueryJoin queryJoin) {
        this.joinExpression = queryJoin.joinExpression();
        this.joinType = queryJoin.joinType();
        this.order = queryJoin.order();
        this.tableAlias = queryJoin.tableAlias();
    }

    public QueryJoinModel(QueryJoin queryJoin, String tableName1, String tableId1, String tableName2, String tableId2) {
        if(queryJoin != null ) {
            if(!StringUtils.isEmpty(queryJoin.joinExpression())) {
                this.joinExpression = queryJoin.joinExpression();
            } else {
                this.joinExpression =  tableName1 + "." + tableId1 + " = " + tableName2 + "." + tableId2;
            }
            this.joinType = queryJoin.joinType();
            this.order = queryJoin.order();
        } else {
            this.joinExpression =  tableName1 + "." + tableId1 + " = " + tableName2 + "." + tableId2;
            this.joinType = QueryJoin.JoinType.LEFT;
            this.order = 1;
        }
    }

    public String getJoinExpression() {
        return joinExpression;
    }

    public void setJoinExpression(String joinExpression) {
        this.joinExpression = joinExpression;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public QueryJoin.JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(QueryJoin.JoinType joinType) {
        this.joinType = joinType;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }
}
