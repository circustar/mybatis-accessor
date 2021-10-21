package com.circustar.mybatis_accessor.provider;

import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DefaultDeleteByIdProcessorProvider extends DefaultDeleteProcessorProvider {

    public DefaultDeleteByIdProcessorProvider(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected Object convertToUpdateTarget(DtoClassInfo dtoClassInfo, Object obj) {
        return obj;
    }

    @Override
    protected List convertToSubUpdateList(DtoClassInfoHelper dtoClassInfoHelper, DtoClassInfo dtoClassInfo, List dtoList) {
        List result = new ArrayList();
        Method readMethod = dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();
        for(Object obj : dtoList) {
            result.add(FieldUtils.getFieldValue(obj, readMethod));
        }
        return result;
    }

    @Override
    protected Object getUpdateId(Object obj, DtoField keyField) {
        return obj;
    }
}
