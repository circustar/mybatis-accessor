package com.circustar.mvcenhance.enhance.mybatisplus.enhancer;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.circustar.mvcenhance.enhance.relation.TableJoinInfo;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableInfoUtils {
    public static TableInfo getTableInfo(Class<?> clazz, Configuration configuration, Package scanPackage) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
        if (tableInfo != null) {
            return tableInfo;
        }
        if (scanPackage == null) {
            return null;
        }
        try {
            BaseMapperScanner.scan(scanPackage);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Class<? extends BaseMapper> mapper = BaseMapperScanner.getBaseMapper(clazz);
        MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(configuration, mapper.getSimpleName());
        return TableInfoHelper.initTableInfo(mapperBuilderAssistant, clazz);
    }

    public static ResultMapping getResultMapping(Configuration configuration, TableFieldInfo tableFieldInfo) {
        ResultMapping.Builder builder = new ResultMapping.Builder(configuration
                , tableFieldInfo.getProperty(), StringUtils.getTargetColumn(tableFieldInfo.getColumn()), tableFieldInfo.getPropertyType());
        TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
        if (tableFieldInfo.getJdbcType() != null && tableFieldInfo.getJdbcType() != JdbcType.UNDEFINED) {
            builder.jdbcType(tableFieldInfo.getJdbcType());
        }

        if (tableFieldInfo.getTypeHandler() != null && tableFieldInfo.getTypeHandler() != UnknownTypeHandler.class) {
            TypeHandler<?> typeHandler = registry.getMappingTypeHandler(tableFieldInfo.getTypeHandler());
            if (typeHandler == null) {
                typeHandler = registry.getInstance(tableFieldInfo.getPropertyType(), tableFieldInfo.getTypeHandler());
            }
            builder.typeHandler(typeHandler);
        }

        return builder.build();
    }

    public static ResultMapping getNestedResultMapping(Configuration configuration, TableInfo tableInfo, TableJoinInfo tableJoinInfo) {
        ResultMapping.Builder builder = new ResultMapping.Builder(configuration
                , tableJoinInfo.getFieldName(), StringUtils.getTargetColumn(tableJoinInfo.getFieldName()), (Class)tableJoinInfo.getActualType());
        builder.nestedResultMapId(registerResultMapping(configuration, tableInfo, null));
        return builder.build();
    }

    public static String getResultMappingId(TableInfo tableInfo) {
        return tableInfo.getCurrentNamespace() + "." + "mybatis-plus" + "_" + tableInfo.getEntityType().getSimpleName();
    }

    public static String registerResultMapping(Configuration configuration, TableInfo tableInfo, List<TableJoinInfo> tableJoinInfos) {
        String id = getResultMappingId(tableInfo);
        Boolean existResultMap = configuration.getResultMapNames().contains(id);
        if(existResultMap == true) {
            return id;
        }

        List<ResultMapping> resultMappings = new ArrayList();
        if (tableInfo.havePK()) {
            ResultMapping idMapping = (new ResultMapping.Builder(configuration, tableInfo.getKeyProperty(), tableInfo.getKeyColumn(), tableInfo.getKeyType())).flags(Collections.singletonList(ResultFlag.ID)).build();
            resultMappings.add(idMapping);
        }

        if (CollectionUtils.isNotEmpty(tableInfo.getFieldList())) {
            tableInfo.getFieldList().forEach((i) -> {
                resultMappings.add(TableInfoUtils.getResultMapping(configuration, i));
            });
        }

        if (CollectionUtils.isNotEmpty(tableJoinInfos)) {
            for(TableJoinInfo tableJoinInfo : tableJoinInfos) {
                Class clazz = (Class) tableJoinInfo.getActualType();
                TableInfo joinTableInfo = TableInfoHelper.getTableInfo(clazz);
                resultMappings.add(TableInfoUtils.getNestedResultMapping(configuration, joinTableInfo, tableJoinInfo));
            }
        }

        ResultMap resultMap = (new org.apache.ibatis.mapping.ResultMap.Builder(configuration
                , id, tableInfo.getEntityType(), resultMappings)).build();
        configuration.addResultMap(resultMap);
        return id;
    }
}

//
//    public static String getDefaultMapperName(String currentNameSpace, Class clazz) {
//        return currentNameSpace + clazz.getSimpleName();
//    }
//
//    public static String getSchema(Configuration configuration) {
//        GlobalConfig globalConfig = GlobalConfigUtils.getGlobalConfig(configuration);
//        if(globalConfig != null && globalConfig.getDbConfig() != null) {
//            return globalConfig.getDbConfig().getSchema();
//        }
//        return null;
//    }
//
//    public static String getTableName(Configuration configuration, Class<?> clazz) {
//        return getTableName(configuration, clazz, true);
//    }
//    public static String getTableName(Configuration configuration, Class<?> clazz, Boolean withSchema) {
//        GlobalConfig.DbConfig dbConfig = GlobalConfigUtils.getGlobalConfig(configuration).getDbConfig();
//        TableName table = clazz.getAnnotation(TableName.class);
//        String tableName = clazz.getSimpleName();
//        String tablePrefix = dbConfig.getTablePrefix();
//        String schema = dbConfig.getSchema();
//        boolean tablePrefixEffect = true;
//        if (table != null) {
//            if (StringUtils.isNotBlank(table.value())) {
//                tableName = table.value();
//                if (StringUtils.isNotBlank(tablePrefix) && !table.keepGlobalPrefix()) {
//                    tablePrefixEffect = false;
//                }
//            } else {
//                tableName = initTableNameWithDbConfig(tableName, dbConfig);
//            }
//
//            if (StringUtils.isNotBlank(table.schema())) {
//                schema = table.schema();
//            }
//        } else {
//            tableName = initTableNameWithDbConfig(tableName, dbConfig);
//        }
//
//        String targetTableName = tableName;
//        if (StringUtils.isNotBlank(tablePrefix) && tablePrefixEffect) {
//            targetTableName = tablePrefix + tableName;
//        }
//
//        if (StringUtils.isNotBlank(schema)) {
//            targetTableName = schema + "." + targetTableName;
//        }
//        return withSchema?targetTableName:tableName;
//    }
//
//    private static String initTableNameWithDbConfig(String className, GlobalConfig.DbConfig dbConfig) {
//        String tableName = className;
//        if (dbConfig.isTableUnderline()) {
//            tableName = StringUtils.camelToUnderline(className);
//        }
//
//        if (dbConfig.isCapitalMode()) {
//            tableName = tableName.toUpperCase();
//        } else {
//            tableName = StringUtils.firstToLowerCase(tableName);
//        }
//
//        return tableName;
//    }
