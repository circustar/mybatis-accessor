package com.circustar.mvcenhance.enhance.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.circustar.mvcenhance.enhance.mybatisplus.injector.methods.PhysicDelete;
import com.circustar.mvcenhance.enhance.mybatisplus.injector.methods.PhysicDeleteBatchByIds;
import com.circustar.mvcenhance.enhance.mybatisplus.injector.methods.PhysicDeleteById;
import com.circustar.mvcenhance.enhance.mybatisplus.injector.methods.PhysicDeleteByMap;

import java.util.List;

public class PhysicDeleteSqlInjector extends DefaultSqlInjector {
    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        methodList.add(new PhysicDelete());
        methodList.add(new PhysicDeleteBatchByIds());
        methodList.add(new PhysicDeleteById());
        methodList.add(new PhysicDeleteByMap());
        return methodList;
    }
}
