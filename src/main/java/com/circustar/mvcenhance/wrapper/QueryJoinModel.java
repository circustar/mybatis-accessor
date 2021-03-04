package com.circustar.mvcenhance.wrapper;

import com.circustar.mvcenhance.annotation.QueryJoin;
import org.springframework.util.StringUtils;

public class QueryJoinModel {
    private String joinString;
    private int order;
    private QueryJoin.JoinType joinType;

    public QueryJoinModel(String joinString, int order, QueryJoin.JoinType joinType) {
        this.joinString = joinString;
        this.order = order;
        this.joinType = joinType;
    }

    public QueryJoinModel(QueryJoin queryJoin, String tableName1, String tableId1, String tableName2, String tableId2) {
        if(queryJoin != null && !StringUtils.isEmpty(queryJoin.joinString())) {
            this.joinString = queryJoin.joinString();
        } else {
            this.joinString =  tableName1 + "." + tableId1 + " = " + tableName2 + "." + tableId2;
        }
        this.joinType = queryJoin.joinType();
        this.order = queryJoin.order();
    }

    public String getJoinString() {
        return joinString;
    }

    public void setJoinString(String joinString) {
        this.joinString = joinString;
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
}
