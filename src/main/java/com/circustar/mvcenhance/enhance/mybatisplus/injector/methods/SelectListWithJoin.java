package com.circustar.mvcenhance.enhance.mybatisplus.injector.methods;


import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.circustar.mvcenhance.enhance.relation.TableJoinInfo;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SelectListWithJoin extends AbstractMethod {

    public SelectListWithJoin() {
    }

//    @Override
//    public void inject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
//        this.configuration = builderAssistant.getConfiguration();
//        this.builderAssistant = builderAssistant;
//        this.languageDriver = this.configuration.getDefaultScriptingLanguageInstance();
//        //this.builderAssistant = builderAssistant;
//        this.injectMappedStatement(mapperClass, modelClass, tableInfo);
//    }

    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        /* mapper 接口方法名一致 */
        List<TableJoinInfo> tableJoinInfoList = TableJoinInfo.parseTableJoinInfo(modelClass);
        if(tableJoinInfoList == null || tableJoinInfoList.size() == 0) {
            return null;
        }
        String joinString = "";
        String masterTableName = tableInfo.getTableName();
        for(TableJoinInfo tableJoinInfo : tableJoinInfoList) {
            Class clazz = (Class) tableJoinInfo.getActualType();
            TableInfo joinTableInfo = TableInfoHelper.getTableInfo(clazz);
            if(joinTableInfo == null) {
                joinTableInfo = TableInfoHelper.initTableInfo(this.builderAssistant, clazz);
            }
            String strAlias = tableJoinInfo.getJoinTable().alias();
            joinString += " left join " + joinTableInfo.getTableName() + " " + strAlias;
            String joinColumnStr = Arrays.stream(tableJoinInfo.getJoinTable().joinColumns())
                    .map(x -> x.connector().convert((x.withAlias() ? (masterTableName + "."):"") + x.masterColumn(), x.values()))
                    .collect(Collectors.joining(" and "));
            joinString += " on " + joinColumnStr;
        }
        CSSqlMethod sqlMethod = CSSqlMethod.SELECT_LIST_WITH_JOIN;
        String sql = String.format(sqlMethod.getSql(), this.sqlFirst(), this.sqlSelectColumns(tableInfo, true), tableInfo.getTableName(), joinString, this.sqlWhereEntityWrapper(true, tableInfo), this.sqlComment());
        SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, modelClass);
        return this.addSelectMappedStatementForTable(mapperClass
                , CSSqlMethod.SELECT_LIST_WITH_JOIN.getMethod(), sqlSource, tableInfo);
    }

    @Override
    protected String sqlSelectColumns(TableInfo table, boolean queryWrapper) {
        return Arrays.stream(table.getAllSqlSelect().split(",")).map(x -> table.getTableName() + "." + x).collect(Collectors.joining(","));
    }
}
