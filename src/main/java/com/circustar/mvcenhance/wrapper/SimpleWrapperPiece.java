package com.circustar.mvcenhance.wrapper;

import com.circustar.mvcenhance.annotation.Connector;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import org.springframework.util.StringUtils;

public class SimpleWrapperPiece {
    private String columnName;
    private Object value;
    private String connectorString;
    private Integer sortIndex;
    private String sortOrder;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getConnectorString() {
        return connectorString;
    }

    public void setConnectorString(String connectorString) {
        this.connectorString = connectorString;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public WrapperPiece convertToWrapperPiece(EntityDtoServiceRelation relation) {
        assert(columnName != null);
        QueryFieldModel queryFieldModel = null;
        if(!StringUtils.isEmpty(connectorString)) {
            queryFieldModel = new QueryFieldModel(columnName, Connector.valueOf(connectorString));
        }

        OrderFieldModel orderFieldModel = null;
        if(sortIndex != null) {
            orderFieldModel = new OrderFieldModel(columnName, sortIndex, sortOrder);
        }

        return new WrapperPiece(queryFieldModel, orderFieldModel, null, relation.getTableInfo().getTableName(), columnName, value);
    }

}
