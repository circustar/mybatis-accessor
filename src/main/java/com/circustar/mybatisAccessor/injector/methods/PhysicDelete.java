package com.circustar.mybatisAccessor.injector.methods;


import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

public class PhysicDelete extends AbstractMethod {
    public PhysicDelete() {
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        /* mapper 接口方法名一致 */
        String method = "physicDelete";
        SqlSource sqlSource;
        String sql = String.format(SqlMethod.DELETE.getSql(), tableInfo.getTableName(), this.sqlWhereEntityWrapper(true, tableInfo), this.sqlComment());
        sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, modelClass);
        return this.addDeleteMappedStatement(mapperClass, method, sqlSource);
    }

    @Override
    protected String sqlWhereEntityWrapper(boolean newLine, TableInfo table) {
        String sqlScript = table.getAllSqlWhere(false, true, "ew.entity.");
        sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", "ew.entity"), true);
        sqlScript = sqlScript + "\n";
        sqlScript = sqlScript + SqlScriptUtils.convertIf(String.format(SqlScriptUtils.convertIf(" AND", String.format("%s and %s", "ew.nonEmptyOfEntity", "ew.nonEmptyOfNormal"), false) + " ${%s}", "ew.sqlSegment"), String.format("%s != null and %s != '' and %s", "ew.sqlSegment", "ew.sqlSegment", "ew.nonEmptyOfWhere"), true);
        sqlScript = SqlScriptUtils.convertWhere(sqlScript) + "\n";
        sqlScript = sqlScript + SqlScriptUtils.convertIf(String.format(" ${%s}", "ew.sqlSegment"), String.format("%s != null and %s != '' and %s", "ew.sqlSegment", "ew.sqlSegment", "ew.emptyOfWhere"), true);
        sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", "ew"), true);
        return newLine ? "\n" + sqlScript : sqlScript;
    }
}
