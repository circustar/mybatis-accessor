package com.circustar.mybatis_accessor.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.methods.*;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.circustar.mybatis_accessor.mapper.CommonMapper;
import com.circustar.mybatis_accessor.mapper.SelectMapper;
import com.circustar.mybatis_accessor.utils.TableInfoUtils;
import com.circustar.mybatis_accessor.injector.methods.*;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnhanceSqlInjector extends DefaultSqlInjector {
    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList;
        if(SelectMapper.class.isAssignableFrom(mapperClass)) {
            Stream.Builder builder = Stream.builder().add(new SelectCount()).add(new SelectMaps())
                    .add(new SelectObjs()).add(new SelectList());
            if (tableInfo.havePK()) {
                builder.add(new SelectById()).add(new SelectBatchByIds());
            }
            methodList = (List<AbstractMethod>) builder.build().collect(Collectors.toList());
        } else {
            methodList = super.getMethodList(mapperClass, tableInfo);
        }
        if(CommonMapper.class.isAssignableFrom(mapperClass)) {
            methodList.add(new SelectListWithJoin());
            methodList.add(new SelectPageWithJoin());
            methodList.add(new SelectCountWithJoin());
        }
        return methodList;
    }

    @Override
    public void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {
        if(CommonMapper.class.isAssignableFrom(mapperClass) && !SelectMapper.class.isAssignableFrom(mapperClass)) {
            TableInfoUtils.initAllTableInfo(builderAssistant.getConfiguration());
        }
        super.inspectInject(builderAssistant, mapperClass);
    }
}
