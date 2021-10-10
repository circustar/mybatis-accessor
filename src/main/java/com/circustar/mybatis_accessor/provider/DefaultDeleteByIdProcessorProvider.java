package com.circustar.mybatis_accessor.provider;

import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.command.DeleteByIdCommand;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.provider.parameter.IEntityProviderParam;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class DefaultDeleteByIdProcessorProvider extends DefaultDeleteProcessorProvider {

    public DefaultDeleteByIdProcessorProvider(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected Object convertToUpdateTarget(DtoClassInfoHelper dtoClassInfoHelper, DtoClassInfo dtoClassInfo, Object obj) {
        return obj;
    }

    @Override
    protected List convertToSubUpdateList(DtoClassInfoHelper dtoClassInfoHelper, DtoClassInfo dtoClassInfo, List dtoList) {
        List result = new ArrayList();
        for(Object obj : dtoList) {
            result.add(FieldUtils.getFieldValue(obj, dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod()));
        }
        return result;
    }

    @Override
    protected Object getUpdateId(Object obj, DtoField keyField) {
        return obj;
    }

    @Override
    protected IUpdateCommand getUpdateCommand() {
        return DeleteByIdCommand.getInstance();
    }

    @Override
    protected Object createUpdateProcessorParam(DtoClassInfo dtoClassInfo, IEntityProviderParam options) {
        return dtoClassInfo.isPhysicDelete();
    }
}
