package com.circustar.mybatis_accessor.model;

import com.circustar.mybatis_accessor.annotation.dto.QueryOrder;
import com.circustar.mybatis_accessor.class_info.DtoField;
import org.springframework.util.StringUtils;

public class QueryOrderModel {
    private String orderExpression;
    private int sortIndex;
    private String sortOrder;

    public QueryOrderModel(String orderExpression, int sortIndex, String sortOrder) {
        this.orderExpression = orderExpression;
        this.sortIndex = sortIndex;
        this.sortOrder = sortOrder;
    }
    public QueryOrderModel(DtoField queryField) {
        QueryOrder queryOrder = queryField.getQueryOrder();
        String tableShortName = queryField.getDtoClassInfo().getEntityClassInfo().getTableInfo().getTableName();
        tableShortName = tableShortName.substring(tableShortName.lastIndexOf(".") + 1);
        this.orderExpression = StringUtils.hasLength(queryOrder.expression()) ? queryOrder.expression()
                : tableShortName + "." + queryField.getEntityFieldInfo().getColumnName();
        this.sortIndex = queryOrder.sortIndex();
        this.sortOrder = queryOrder.sortOrder();
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
