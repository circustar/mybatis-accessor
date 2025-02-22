package com.circustar.mybatis_accessor.model;

import com.circustar.mybatis_accessor.annotation.Connector;
import com.circustar.mybatis_accessor.annotation.dto.QueryWhere;
import com.circustar.mybatis_accessor.class_info.DtoField;
import org.springframework.util.StringUtils;

public class QueryWhereModel {
    private String tableColumn;
    private boolean dynamicTableColumn;
    private String expression;
    private Connector connector;
    private DtoField dtoField;

    public QueryWhereModel(QueryWhere queryWhere, String tableName, DtoField dtoField) {
        this(queryWhere, tableName, dtoField, Connector.EQ);
    }
    public QueryWhereModel(QueryWhere queryWhere, String tableName, DtoField dtoField, Connector connector) {
        this.dtoField = dtoField;
        final String tableShortName = tableName.substring(tableName.lastIndexOf(".") + 1);
        if(queryWhere != null) {
            this.expression = queryWhere.expression();
            this.connector = queryWhere.connector();
            this.dynamicTableColumn = queryWhere.dynamicTableColumn();

            if(StringUtils.hasLength(queryWhere.tableColumn())) {
                this.tableColumn = queryWhere.tableColumn();
                if(!this.dynamicTableColumn && queryWhere.convertColumnToUnderline()) {
                    this.tableColumn = com.circustar.common_utils.collection.StringUtils.c2l(this.tableColumn);
                }
            } else {
                this.tableColumn = tableShortName + "." + dtoField.getEntityFieldInfo().getColumnName();
            }

        } else {
            this.tableColumn = tableShortName + "." + dtoField.getEntityFieldInfo().getColumnName();
            this.connector = connector;
            this.dynamicTableColumn = false;
        }
    }

    public String getExpression() {
        return expression;
    }

    public Connector getConnector() {
        return connector;
    }

    public DtoField getDtoField() {
        return dtoField;
    }

    public String getTableColumn() {
        return tableColumn;
    }

    public boolean isDynamicTableColumn() {
        return dynamicTableColumn;
    }
}
