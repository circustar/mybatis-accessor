package com.circustar.mvcenhance.enhance.mybatisplus.injector.methods;


import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.TableInfoUtils;
import com.circustar.mvcenhance.enhance.relation.TableJoinInfo;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private String resultMap;

    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        /* mapper 接口方法名一致 */
        //TODO:getFrom ExTableInfo
        List<TableJoinInfo> tableJoinInfoList = TableJoinInfo.parseTableJoinInfo(modelClass);
        if(tableJoinInfoList == null || tableJoinInfoList.size() == 0) {
            return null;
        }

        String joinString = "";
        String masterTableName = tableInfo.getTableName();
        List<String> subDtoColumns = new ArrayList<>();
        for(TableJoinInfo tableJoinInfo : tableJoinInfoList) {
            Class clazz = (Class) tableJoinInfo.getActualType();

            TableInfo joinTableInfo = TableInfoUtils.getTableInfo(clazz, this.configuration, mapperClass.getPackage());
            String strAlias = tableJoinInfo.getJoinTable().alias();
            joinString += " left join " + joinTableInfo.getTableName() + " " + strAlias;
            String joinColumnStr = Arrays.stream(tableJoinInfo.getJoinTable().joinColumns())
                    .map(x -> x.connector().convert((x.withAlias() ? (masterTableName + "."):"") + x.masterColumn(), x.values()))
                    .collect(Collectors.joining(" and "));
            joinString += " on " + joinColumnStr;

            String joinColumns = Arrays.stream(joinTableInfo.getAllSqlSelect().split(","))
                    .map(x -> strAlias + "." + x ).collect(Collectors.joining(",")); // + " as " +  strAlias + "_" + x
            subDtoColumns.add(joinColumns);
        }
        String allColumns = this.sqlSelectColumns(tableInfo, true);
        if(subDtoColumns != null && subDtoColumns.size() > 0) {
            allColumns += "," + subDtoColumns.stream().collect(Collectors.joining(","));
        }
        CSSqlMethod sqlMethod = CSSqlMethod.SELECT_LIST_WITH_JOIN;
        String sql = String.format(sqlMethod.getSql(), this.sqlFirst(), allColumns, tableInfo.getTableName(), joinString, this.sqlWhereEntityWrapper(true, tableInfo), this.sqlComment());
        SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, modelClass);
//        ExTableInfo exTableInfo = new ExTableInfo(tableInfo);
        this.resultMap = TableInfoUtils.registerResultMapping(configuration, tableInfo, tableJoinInfoList);
        MappedStatement ms = this.addSelectMappedStatementForTable(mapperClass
                , CSSqlMethod.SELECT_LIST_WITH_JOIN.getMethod(), sqlSource, tableInfo);
        //ms.getResultMaps().get(0).getPropertyResultMappings().add(null);
        return ms;
    }

    @Override
    protected String sqlSelectColumns(TableInfo table, boolean queryWrapper) {
        return Arrays.stream(table.getAllSqlSelect().split(",")).map(x -> table.getTableName() + "." + x).collect(Collectors.joining(","));
    }
//
    @Override
    protected MappedStatement addSelectMappedStatementForTable(Class<?> mapperClass, String id, SqlSource sqlSource, TableInfo table) {
        String resultMap = this.resultMap;
        MappedStatement mappedStatement =  this.addMappedStatement(mapperClass, id, sqlSource
                , SqlCommandType.SELECT, (Class)null, resultMap
                , (Class)null, new NoKeyGenerator(), (String)null, (String)null);

        return mappedStatement;
    }

//    private String getResultMappingId(TableInfo tableInfo) {
//        return tableInfo.getCurrentNamespace() + ".with_join_" + tableInfo.getEntityType().getSimpleName();
//    }

//    protected String createResultMap(TableInfo tableInfo, List<TableJoinInfo> tableJoinInfos) {
//        String id = getResultMappingId(tableInfo);
//        List<ResultMapping> resultMappings = new ArrayList();
//        if (tableInfo.havePK()) {
//            ResultMapping idMapping = (new ResultMapping.Builder(this.configuration
//                    , tableInfo.getKeyProperty(), tableInfo.getKeyColumn()
//                    , tableInfo.getKeyType())).flags(Collections.singletonList(ResultFlag.ID)).build();
//            resultMappings.add(idMapping);
//        }
//
//        if (CollectionUtils.isNotEmpty(tableInfo.getFieldList())) {
//            tableInfo.getFieldList().forEach((i) -> {
//                ResultMapping resultMapping = TableInfoUtils.getResultMapping(this.configuration, i);
////                resultMappings.add(i.getResultMapping(this.configuration));
//                resultMappings.add(resultMapping);
//            });
//        }
//
//        if (CollectionUtils.isNotEmpty(tableJoinInfos)) {
//            for(TableJoinInfo tableJoinInfo : tableJoinInfos) {
//                Class clazz = (Class) tableJoinInfo.getActualType();
//                TableInfo joinTableInfo = TableInfoHelper.getTableInfo(clazz);
//                resultMappings.add(TableInfoUtils.getResultMapping(this.configuration, joinTableInfo, tableJoinInfo));
//            }
//        }
//
//        ResultMap resultMap = (new org.apache.ibatis.mapping.ResultMap.Builder(this.configuration
//                , id, tableInfo.getEntityType(), resultMappings)).build();
//        this.configuration.addResultMap(resultMap);
//        return id;
//    }
}
