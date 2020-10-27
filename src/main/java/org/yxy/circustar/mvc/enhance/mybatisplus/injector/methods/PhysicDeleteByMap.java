package org.yxy.circustar.mvc.enhance.mybatisplus.injector.methods;


import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.Map;

public class PhysicDeleteByMap extends AbstractMethod {
    public PhysicDeleteByMap() {
    }

    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String method = "physicDeleteByMap";
        SqlSource sqlSource;
        String sql = String.format(SqlMethod.DELETE_BY_MAP.getSql(), tableInfo.getTableName(), this.sqlWhereByMap(tableInfo));
        sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, Map.class);
        return this.addDeleteMappedStatement(mapperClass, method, sqlSource);
    }
}
