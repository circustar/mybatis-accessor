package com.circustar.mybatis_accessor.wrapper;

import com.circustar.mybatis_accessor.annotation.Connector;
import com.circustar.mybatis_accessor.annotation.QueryWhere;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import org.springframework.util.StringUtils;

public class QueryWhereModel {
    public QueryWhereModel(QueryWhere queryWhere, String table_name, DtoField dtoField) {
        this(queryWhere, table_name, dtoField, Connector.eq);
    }
    public QueryWhereModel(QueryWhere queryWhere, String table_name, DtoField dtoField, Connector connector) {
        this.dtoField = dtoField;
        if(queryWhere != null) {
            if(StringUtils.isEmpty(queryWhere.expression())) {
                this.expression = table_name + "." + dtoField.getEntityFieldInfo().getColumnName();
            } else {
                this.expression = queryWhere.expression();
            }
            this.connector = queryWhere.connector();
        } else {
            this.expression = table_name + "." + dtoField.getEntityFieldInfo().getColumnName();
            this.connector = connector;
        }
    }

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
}
