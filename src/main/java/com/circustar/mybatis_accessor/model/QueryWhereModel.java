package com.circustar.mybatis_accessor.model;

import com.circustar.mybatis_accessor.annotation.Connector;
import com.circustar.mybatis_accessor.annotation.dto.QueryWhere;
import com.circustar.mybatis_accessor.class_info.DtoField;
import org.springframework.util.StringUtils;

public class QueryWhereModel {
    private String tableColumn;
    private String expression;
    private Connector connector;
    private DtoField dtoField;

    public QueryWhereModel(QueryWhere queryWhere, String tableName, DtoField dtoField) {
        this(queryWhere, tableName, dtoField, Connector.EQ);
    }
    public QueryWhereModel(QueryWhere queryWhere, String tableName, DtoField dtoField, Connector connector) {
        this.dtoField = dtoField;
        if(queryWhere != null) {
            if(StringUtils.hasLength(queryWhere.tableColumn())) {
                this.tableColumn = queryWhere.tableColumn();
            } else {
                this.tableColumn = tableName + "." + dtoField.getEntityFieldInfo().getColumnName();
            }
            this.expression = queryWhere.expression();
            this.connector = queryWhere.connector();
        } else {
            this.tableColumn = tableName + "." + dtoField.getEntityFieldInfo().getColumnName();
            this.connector = connector;
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
}
