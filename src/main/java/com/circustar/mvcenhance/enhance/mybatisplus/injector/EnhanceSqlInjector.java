package com.circustar.mvcenhance.enhance.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.TableInfoUtils;
import com.circustar.mvcenhance.enhance.mybatisplus.injector.methods.*;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.util.List;
import java.util.Set;

public class EnhanceSqlInjector extends DefaultSqlInjector {
    private static final Log logger = LogFactory.getLog(EnhanceSqlInjector.class);

    public EnhanceSqlInjector() {
        super();
        //TableInfoUtils.initAllTableInfo();
    }

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        methodList.add(new PhysicDelete());
        methodList.add(new PhysicDeleteBatchByIds());
        methodList.add(new PhysicDeleteById());
        methodList.add(new PhysicDeleteByMap());
        methodList.add(new SelectListWithJoin());
        methodList.add(new SelectPageWithJoin());
        return methodList;
    }

    @Override
    public void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {
        TableInfoUtils.initAllTableInfo(builderAssistant.getConfiguration());
        super.inspectInject(builderAssistant, mapperClass);
    }

//    @Override
//    public void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {
//        Class<?> modelClass = this.extractModelClass(mapperClass);
//        if (modelClass != null) {
//            String className = mapperClass.toString();
//            Set<String> mapperRegistryCache = GlobalConfigUtils.getMapperRegistryCache(builderAssistant.getConfiguration());
//            if (!mapperRegistryCache.contains(className)) {
//                List<AbstractMethod> methodList = this.getMethodList(mapperClass);
//                if (CollectionUtils.isNotEmpty(methodList)) {
//                    TableInfo tableInfo = ExTableInfoHelper.initTableInfo(builderAssistant, modelClass);
//                    methodList.forEach((m) -> {
//                        m.inject(builderAssistant, mapperClass, modelClass, tableInfo);
//                    });
//                } else {
//                    logger.debug(mapperClass.toString() + ", No effective injection method was found.");
//                }
//
//                mapperRegistryCache.add(className);
//            }
//        }
//    }
}
