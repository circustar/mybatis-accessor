package com.circustar.mvcenhance.enhance.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.TableInfoUtils;
import com.circustar.mvcenhance.enhance.mybatisplus.injector.methods.*;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.util.List;

public class EnhanceSqlInjector extends DefaultSqlInjector {
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
}
