package com.circustar.mybatis_accessor.injector.methods;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

public class SelectCountWithJoin extends SelectListWithJoin {

    public SelectCountWithJoin() {
    }

    protected CSSqlMethod getSqlMethod() {
        return CSSqlMethod.SELECT_COUNT_WITH_JOIN;
    }

    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        /* mapper 接口方法名一致 */
        String joinTable = " ${" + MvcEnhanceConstants.MYBATIS_ENHANCE_JOIN_TABLE + "} ";

        CSSqlMethod sqlMethod = this.getSqlMethod();
        String sql = String.format(sqlMethod.getSql(), this.sqlFirst(), tableInfo.getTableName(), joinTable, this.sqlWhereEntityWrapper(true, tableInfo), this.sqlComment());
        SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, modelClass);

        return this.addSelectMappedStatementForOther(mapperClass, sqlMethod.getMethod(), sqlSource, Integer.class);
    }
}
