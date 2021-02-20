package com.circustar.mvcenhance.utils;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.circustar.mvcenhance.classInfo.TableJoinInfo;
import com.circustar.mvcenhance.scanner.BaseMapperScanner;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class TableInfoUtils {
    private static volatile boolean allTableInfoInitialized = false;
    public static AtomicReference<List<String>> scanPackages = new AtomicReference<>();
    private static boolean userCamelCase = false;
    public static synchronized void initAllTableInfo(Configuration configuration) {
        if(allTableInfoInitialized) {
            return;
        }
        List<String> pks = scanPackages.get();
        for (String scanPackage : pks) {
            initPackageTableInfo(configuration, scanPackage);
        }
        userCamelCase = configuration.isMapUnderscoreToCamelCase();
        allTableInfoInitialized = true;
    }

    private static void initPackageTableInfo(Configuration configuration, String scanPackage) {
        if(BaseMapperScanner.packageScanned(scanPackage)) {
            return;
        }
        try {
            BaseMapperScanner.scan(scanPackage);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Set<Class<? extends BaseMapper>> baseMapperFromPackage = BaseMapperScanner.getBaseMapperFromPackage(scanPackage);
        for(Class<? extends BaseMapper> mapper : baseMapperFromPackage) {
            MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(configuration, mapper.getSimpleName());
            TableInfoHelper.initTableInfo(mapperBuilderAssistant, (Class)ClassUtils.getFirstTypeArgument(mapper));
        }
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

    public static String getDBObjectName(String entityName) {
        if(userCamelCase) {
            return StringUtils.camelToUnderline(entityName);
        }
        return entityName;
    }
}
