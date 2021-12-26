package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class UpdateExecuteBeanMethodEvent implements IUpdateEvent<UpdateEventModel> {
    private static ApplicationContext applicationContext = null;
    private void setApplicationContext(ApplicationContext applicationContext) {
        UpdateExecuteBeanMethodEvent.applicationContext = applicationContext;
    }

    @Override
    public ExecuteTiming getDefaultExecuteTiming() {
        return ExecuteTiming.AFTER_ENTITY_UPDATE;
    }

    @Override
    public IUpdateCommand.UpdateType[] getDefaultUpdateTypes() {
        return new IUpdateCommand.UpdateType[]{IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE
                , IUpdateCommand.UpdateType.DELETE};
    }

    @Override
    public void exec(UpdateEventModel model, IUpdateCommand.UpdateType updateType, DtoClassInfo dtoClassInfo
            , List<Object> dtoList, String updateId, int level) {
        if(UpdateExecuteBeanMethodEvent.applicationContext == null) {
            setApplicationContext(dtoClassInfo.getDtoClassInfoHelper().getApplicationContext());
        }
        try {
            String beanName = model.getUpdateParams().get(0);
            Object bean = applicationContext.getBean(beanName);
            String methodName = model.getUpdateParams().get(1);

            List<DtoField> paramField = new ArrayList<>();
            List<Class> paramClassList = new ArrayList<>();
            if(model.getUpdateParams().size() > 2) {
                for (int j = 2; j < model.getUpdateParams().size(); j++) {
                    if(StringUtils.hasLength(model.getUpdateParams().get(j))) {
                        DtoField dtoField = dtoClassInfo.getDtoField(model.getUpdateParams().get(j));
                        paramField.add(dtoField);
                        paramClassList.add(dtoField.getOwnClass());
                    }
                }
            }
            Method method;
            if(paramClassList.size() > 0) {
                method = bean.getClass().getDeclaredMethod(methodName, paramClassList.toArray(new Class[0]));
            } else {
                method = bean.getClass().getDeclaredMethod(methodName, dtoClassInfo.getDtoClass());
            }

            for(Object dto : dtoList) {
                List<Object> methodParams = new ArrayList<>();
                if(paramField.size() > 0) {
                    for (int j = 0; j < paramField.size(); j++) {
                        methodParams.add(FieldUtils.getFieldValue(dto, paramField.get(j).getPropertyDescriptor().getReadMethod()));
                    }
                } else {
                    methodParams.add(dto);
                }
                method.invoke(bean, methodParams.toArray());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
