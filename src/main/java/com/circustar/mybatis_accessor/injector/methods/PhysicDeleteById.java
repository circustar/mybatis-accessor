package com.circustar.mybatis_accessor.injector.methods;


import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

public class PhysicDeleteById extends PhysicDelete {
    private static final long serialVersionUID = -1L;
    public PhysicDeleteById() {
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String method = "physicDeleteById";
        SqlSource sqlSource;
        String sql = String.format(SqlMethod.DELETE_BY_ID.getSql(), tableInfo.getTableName(), tableInfo.getKeyColumn(), tableInfo.getKeyProperty());
        sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, Object.class);
        return this.addDeleteMappedStatement(mapperClass, method, sqlSource);
    }
}

