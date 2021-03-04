package com.circustar.mvcenhance.wrapper;

import com.circustar.mvcenhance.annotation.Connector;
import com.circustar.mvcenhance.annotation.QueryWhere;
import com.circustar.mvcenhance.classInfo.TableFieldInfo;
import org.springframework.util.StringUtils;

public class QueryWhereModel {
    public QueryWhereModel(QueryWhere queryWhere, String table_name, TableFieldInfo tableFieldInfo) {
        this(queryWhere, table_name, tableFieldInfo, Connector.eq);
    }
    public QueryWhereModel(QueryWhere queryWhere, String table_name, TableFieldInfo tableFieldInfo, Connector connector) {
        this.tableFieldInfo = tableFieldInfo;
        if(queryWhere != null) {
            if(StringUtils.isEmpty(queryWhere.expression())) {
                this.expression = table_name + "_" + tableFieldInfo.getColumnName();
            }
            this.expression = queryWhere.expression();
            this.connector = queryWhere.connector();
        } else {
            this.expression = table_name + "_" + tableFieldInfo.getColumnName();
            this.connector = connector;
        }
    }

    private String expression;
    private Connector connector;
    private TableFieldInfo tableFieldInfo;

    public String getExpression() {
        return expression;
    }

    public Connector getConnector() {
        return connector;
    }

    public TableFieldInfo getTableFieldInfo() {
        return tableFieldInfo;
    }
}
