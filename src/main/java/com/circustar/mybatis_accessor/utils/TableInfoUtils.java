package com.circustar.mybatis_accessor.utils;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.circustar.common_utils.reflection.ClassUtils;
import com.circustar.mybatis_accessor.class_info.TableJoinInfo;
import com.circustar.mybatis_accessor.scanner.BaseMapperScanner;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TableInfoUtils {
    private final static String DEFAULT_NAMESPACE = "CCS.";
    private final static String DEFAULT_NESTED_NAMESPACE = "N_CCS_";
    private final static String DEFAULT_MYBATIS_PLUS_NAMESPACE = "mybatis-plus_";
    private static Boolean allTableInfoInitialized = false;
    public final static AtomicReference<List<String>> SCAN_PACKAGES = new AtomicReference<>();
    private static boolean userCamelCase;
    private static TypeHandlerRegistry typeHandlerRegistry;
    private static Lock lock = new ReentrantLock();
    public static void initAllTableInfo(Configuration configuration) {
        if (allTableInfoInitialized) {
            return;
        }
        if(lock.tryLock()) {
            try {
                TableInfoUtils.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                List<String> pks = SCAN_PACKAGES.get();
                for (String scanPackage : pks) {
                    initPackageTableInfo(configuration, scanPackage);
                }
                userCamelCase = configuration.isMapUnderscoreToCamelCase();
                allTableInfoInitialized = true;
            } finally {
                lock.unlock();
            }
        }
    }

    public static boolean isMybatisSupportType(Class type) {
        return TableInfoUtils.typeHandlerRegistry.getTypeHandler(type) != null;
    }

    private static void initPackageTableInfo(Configuration configuration, String scanPackage) {
        if(BaseMapperScanner.packageScanned(scanPackage)) {
            return;
        }
        try {
            BaseMapperScanner.scan(scanPackage);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Set<Class<? extends BaseMapper>> baseMapperFromPackage = BaseMapperScanner.getBaseMapperFromPackage(scanPackage);
        for(Class<? extends BaseMapper> mapper : baseMapperFromPackage) {
            MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(configuration, mapper.getSimpleName());
            TableInfoHelper.initTableInfo(mapperBuilderAssistant, (Class) ClassUtils.getFirstTypeArgument(mapper));
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

    private static ResultMapping getNestedResultMapping(Configuration configuration
            , TableInfo tableInfo
            , TableJoinInfo tableJoinInfo) {
        ResultMapping.Builder builder = new ResultMapping.Builder(configuration, tableJoinInfo.getFieldName()
                , StringUtils.getTargetColumn(tableJoinInfo.getFieldName()), tableJoinInfo.getOwnerClass());
//        String nestedId = registerResultMapping(configuration, tableInfo, null);
        String nestedId = registerResultMapping(configuration, tableInfo, null, DEFAULT_NESTED_NAMESPACE);
        builder.nestedResultMapId(nestedId);
        builder.columnPrefix(TableJoinColumnPrefixManager.tryGet(tableJoinInfo.getTargetClass()
                , tableJoinInfo.getActualClass(), tableJoinInfo.getPosition()) + "_");
        return builder.build();
    }

    public static String getResultMappingId(TableInfo tableInfo, String namespace) {
        return (StringUtils.isBlank(namespace) ? DEFAULT_NAMESPACE : namespace)
                + DEFAULT_MYBATIS_PLUS_NAMESPACE + tableInfo.getEntityType().getSimpleName();
    }

    public static String registerResultMapping(Configuration configuration, TableInfo tableInfo
            , List<TableJoinInfo> tableJoinInfos) {
        return registerResultMapping(configuration, tableInfo, tableJoinInfos, tableInfo.getCurrentNamespace());
    }
//    public static boolean findJoinTableListLoop(List<Class> joinTableList) {
//        if(joinTableList == null || joinTableList.size() == 1) {
//            return false;
//        }
//        int lastPos = joinTableList.size() - 1;
//        Class lastClass = joinTableList.get(lastPos);
//        for(int i = lastPos - 1; i > 0; i--) {
//            Class checkClass = joinTableList.get(i);
//            if(checkClass.equals(lastClass)) {
//                int j = lastPos - 1;
//                int k = i - 1;
//                for(;j > i && k >= 0; j--,k--) {
//                    if(!joinTableList.get(j).equals(joinTableList.get(k))) {
//                        return false;
//                    }
//                }
//                return j == i;
//            }
//        }
//        return false;
//    }

    public static String registerResultMapping(Configuration configuration, TableInfo tableInfo
            , List<TableJoinInfo> tableJoinInfos, String namespace) {
        String id = getResultMappingId(tableInfo, namespace);
        Boolean existResultMap = configuration.getResultMapNames().contains(id);
        if(existResultMap == true) {
            return id;
        }

        List<ResultMapping> resultMappings = new ArrayList();
        if (tableInfo.havePK()) {
            ResultMapping idMapping = new ResultMapping.Builder(configuration, tableInfo.getKeyProperty()
                    , tableInfo.getKeyColumn(), tableInfo.getKeyType())
                    .flags(Collections.singletonList(ResultFlag.ID)).build();
            resultMappings.add(idMapping);
        }

        if (CollectionUtils.isNotEmpty(tableInfo.getFieldList())) {
            tableInfo.getFieldList().forEach((i) -> {
                resultMappings.add(TableInfoUtils.getResultMapping(configuration, i));
            });
        }

        if (CollectionUtils.isNotEmpty(tableJoinInfos)) {
            for(TableJoinInfo tableJoinInfo : tableJoinInfos) {
                Class clazz = tableJoinInfo.getActualClass();
                TableInfo joinTableInfo = TableInfoHelper.getTableInfo(clazz);
                resultMappings.add(TableInfoUtils.getNestedResultMapping(configuration, joinTableInfo, tableJoinInfo));
            }
        }

        existResultMap = configuration.getResultMapNames().contains(id);
        if(existResultMap == true) {
            return id;
        }
        ResultMap resultMap = new ResultMap.Builder(configuration
                , id, tableInfo.getEntityType(), resultMappings).build();
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
