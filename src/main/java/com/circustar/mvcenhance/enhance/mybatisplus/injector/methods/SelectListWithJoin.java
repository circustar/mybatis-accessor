package com.circustar.mvcenhance.enhance.mybatisplus.injector.methods;


import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.MvcEnhanceConstants;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.TableInfoUtils;
import com.circustar.mvcenhance.enhance.relation.TableJoinInfo;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SelectListWithJoin extends AbstractMethod {

    public SelectListWithJoin() {
    }

    protected CSSqlMethod getSqlMethod() {
        return CSSqlMethod.SELECT_LIST_WITH_JOIN;
    }

    private String resultMap;

    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        /* mapper 接口方法名一致 */
        List<TableJoinInfo> tableJoinInfoList = TableJoinInfo.parseEntityTableJoinInfo(this.configuration, modelClass);
        if(tableJoinInfoList == null || tableJoinInfoList.size() == 0) {
            return null;
        }
        String joinTable = " ${" + MvcEnhanceConstants.MYBATIS_ENHANCE_JOIN_TABLE + "} ";
        String joinColumn = " ${" + MvcEnhanceConstants.MYBATIS_ENHANCE_JOIN_COLUMNS + "} ";

        String allColumns = this.sqlSelectColumns(tableInfo, true) + joinColumn;
        CSSqlMethod sqlMethod = this.getSqlMethod();
        String sql = String.format(sqlMethod.getSql(), this.sqlFirst(), allColumns, tableInfo.getTableName(), joinTable, this.sqlWhereEntityWrapper(true, tableInfo), this.sqlComment());
        SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, modelClass);
        this.resultMap = TableInfoUtils.registerResultMapping(configuration, tableInfo, tableJoinInfoList);
        MappedStatement ms = this.addSelectMappedStatementForTable(mapperClass
                , this.getSqlMethod().getMethod(), sqlSource, tableInfo);
        return ms;
    }

    @Override
    protected String sqlWhereEntityWrapper(boolean newLine, TableInfo table) {
        String sqlScript;

        if (table.isWithLogicDelete()) {
            String logicDeleteSql = table.getTableName() + "." + table.getLogicDeleteFieldInfo().getColumn() + " = " + table.getLogicDeleteFieldInfo().getLogicNotDeleteValue();
            sqlScript = table.getAllSqlWhere(true, true, "ew.entity.");
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", "ew.entity"), true);
            sqlScript = sqlScript + "\n" + " and " + logicDeleteSql + "\n"; // add deleteValue
            String normalSqlScript = SqlScriptUtils.convertIf(String.format("AND ${%s}", "ew.sqlSegment"), String.format("%s != null and %s != '' and %s", "ew.sqlSegment", "ew.sqlSegment", "ew.nonEmptyOfNormal"), true);
            normalSqlScript = normalSqlScript + "\n";
            normalSqlScript = normalSqlScript + SqlScriptUtils.convertIf(String.format(" ${%s}", "ew.sqlSegment"), String.format("%s != null and %s != '' and %s", "ew.sqlSegment", "ew.sqlSegment", "ew.emptyOfNormal"), true);
            sqlScript = sqlScript + normalSqlScript;
            sqlScript = SqlScriptUtils.convertChoose(String.format("%s != null", "ew"), sqlScript, logicDeleteSql);
            sqlScript = SqlScriptUtils.convertWhere(sqlScript);
            return newLine ? "\n" + sqlScript : sqlScript;
        } else {
            sqlScript = table.getAllSqlWhere(false, true, "ew.entity.");
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", "ew.entity"), true);
            sqlScript = sqlScript + "\n";
            sqlScript = sqlScript + SqlScriptUtils.convertIf(String.format(SqlScriptUtils.convertIf(" AND", String.format("%s and %s", "ew.nonEmptyOfEntity", "ew.nonEmptyOfNormal"), false) + " ${%s}", "ew.sqlSegment"), String.format("%s != null and %s != '' and %s", "ew.sqlSegment", "ew.sqlSegment", "ew.nonEmptyOfWhere"), true);
            sqlScript = SqlScriptUtils.convertWhere(sqlScript) + "\n";
            sqlScript = sqlScript + SqlScriptUtils.convertIf(String.format(" ${%s}", "ew.sqlSegment"), String.format("%s != null and %s != '' and %s", "ew.sqlSegment", "ew.sqlSegment", "ew.emptyOfWhere"), true);
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", "ew"), true);
            return newLine ? "\n" + sqlScript : sqlScript;
        }
    }

    @Override
    protected String sqlSelectColumns(TableInfo table, boolean queryWrapper) {
        return Arrays.stream(table.getAllSqlSelect().split(",")).map(x -> table.getTableName() + "." + x).collect(Collectors.joining(","));
    }

    @Override
    protected MappedStatement addSelectMappedStatementForTable(Class<?> mapperClass, String id, SqlSource sqlSource, TableInfo table) {
        String resultMap = this.resultMap;
        MappedStatement mappedStatement =  this.addMappedStatement(mapperClass, id, sqlSource
                , SqlCommandType.SELECT, (Class)null, resultMap
                , (Class)null, new NoKeyGenerator(), (String)null, (String)null);

        return mappedStatement;
    }
}