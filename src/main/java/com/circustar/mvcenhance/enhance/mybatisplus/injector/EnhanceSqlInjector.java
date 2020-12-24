package com.circustar.mvcenhance.enhance.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.circustar.mvcenhance.enhance.mybatisplus.injector.methods.*;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.util.List;

public class EnhanceSqlInjector extends DefaultSqlInjector {
    private static final Log logger = LogFactory.getLog(EnhanceSqlInjector.class);

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        methodList.add(new PhysicDelete());
        methodList.add(new PhysicDeleteBatchByIds());
        methodList.add(new PhysicDeleteById());
        methodList.add(new PhysicDeleteByMap());
        methodList.add(new SelectListWithJoin());
        return methodList;
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
