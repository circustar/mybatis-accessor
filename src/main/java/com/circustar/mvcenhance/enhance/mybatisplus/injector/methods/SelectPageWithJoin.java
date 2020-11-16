package com.circustar.mvcenhance.enhance.mybatisplus.injector.methods;


import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

public class SelectPageWithJoin extends AbstractMethod {
    public SelectPageWithJoin() {
    }

    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        /* mapper 接口方法名一致 */
        String method = "selectPageWithJoin";
        SqlSource sqlSource;
        String sql = String.format(SqlMethod.DELETE.getSql(), tableInfo.getTableName(), this.sqlWhereEntityWrapper(true, tableInfo), this.sqlComment());
        sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, modelClass);
        return this.addDeleteMappedStatement(mapperClass, method, sqlSource);
    }
}
