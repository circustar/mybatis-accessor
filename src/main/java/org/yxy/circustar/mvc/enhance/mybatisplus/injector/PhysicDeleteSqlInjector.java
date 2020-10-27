package org.yxy.circustar.mvc.enhance.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import org.yxy.circustar.mvc.enhance.mybatisplus.injector.methods.PhysicDelete;
import org.yxy.circustar.mvc.enhance.mybatisplus.injector.methods.PhysicDeleteBatchByIds;
import org.yxy.circustar.mvc.enhance.mybatisplus.injector.methods.PhysicDeleteById;
import org.yxy.circustar.mvc.enhance.mybatisplus.injector.methods.PhysicDeleteByMap;

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
