package com.circustar.mvcenhance.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

public class PhysicDeleteBatchByIds extends PhysicDelete {
    public PhysicDeleteBatchByIds() {
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String method = "physicDeleteBatchByIds";
        SqlSource sqlSource;
        String sql = String.format(SqlMethod.DELETE_BATCH_BY_IDS.getSql(), tableInfo.getTableName(), tableInfo.getKeyColumn(), SqlScriptUtils.convertForeach("#{item}", "coll", (String)null, "item", ","));
        sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, Object.class);
        return this.addDeleteMappedStatement(mapperClass, method, sqlSource);
    }
}
