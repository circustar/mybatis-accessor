package com.circustar.mybatis_accessor.model;

import com.circustar.mybatis_accessor.annotation.Connector;
import com.circustar.mybatis_accessor.annotation.dto.QueryWhere;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import org.springframework.util.StringUtils;

public class QueryWhereModel {
    public QueryWhereModel(QueryWhere queryWhere, String table_name, DtoField dtoField) {
        this(queryWhere, table_name, dtoField, Connector.eq);
    }
    public QueryWhereModel(QueryWhere queryWhere, String table_name, DtoField dtoField, Connector connector) {
        this.dtoField = dtoField;
        if(queryWhere != null) {
            if(StringUtils.hasLength(queryWhere.tableColumn())) {
                this.tableColumn = queryWhere.tableColumn();
            } else {
                this.tableColumn = table_name + "." + dtoField.getEntityFieldInfo().getColumnName();
            }
            this.expression = queryWhere.expression();
            this.connector = queryWhere.connector();
        } else {
            this.tableColumn = table_name + "." + dtoField.getEntityFieldInfo().getColumnName();
            this.connector = connector;
        }
    }

    private String tableColumn;
    private String expression;
    private Connector connector;
    private DtoField dtoField;

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
