package com.circustar.mvcenhance.enhance.mybatisplus.injector.methods;


import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;

public class SelectByIdWithJoin extends AbstractMethod {
    public SelectByIdWithJoin() {
    }

    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        SqlMethod sqlMethod = SqlMethod.SELECT_BY_ID;
        SqlSource sqlSource = new RawSqlSource(this.configuration, String.format(sqlMethod.getSql(), this.sqlSelectColumns(tableInfo, false), tableInfo.getTableName(), tableInfo.getKeyColumn(), tableInfo.getKeyProperty(), tableInfo.getLogicDeleteSql(true, true)), Object.class);
        return this.addSelectMappedStatementForTable(mapperClass, this.getMethod(sqlMethod), sqlSource, tableInfo);
    }
}
