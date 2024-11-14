package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class UpdateExecuteBeanMethodEvent implements IUpdateEvent<UpdateEventModel> {
    private static ApplicationContext applicationContext;
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
            , List<Object> dtoList, String updateEventLogId) throws MybatisAccessorException {
        if(UpdateExecuteBeanMethodEvent.applicationContext == null) {
            setApplicationContext(dtoClassInfo.getDtoClassInfoHelper().getApplicationContext());
        }
        try {
            String beanName = model.getUpdateParams().get(0);
            Object bean;
            if(applicationContext.containsBean(beanName)) {
                bean = applicationContext.getBean(beanName);
            } else {
                bean = applicationContext.getBean(Class.forName(beanName));
            }

            String methodName = model.getUpdateParams().get(1);

            List<DtoField> paramField = new ArrayList<>();
            List<Class> paramClassList = new ArrayList<>();
            List<Object> staticParam = new ArrayList<>();
            if(model.getUpdateParams().size() > 2) {
                for (int j = 2; j < model.getUpdateParams().size(); j++) {
                    final String updateParam = model.getUpdateParams().get(j);
                    if(StringUtils.hasLength(updateParam)) {
                        if(updateParam.startsWith("'") && updateParam.endsWith("'")) {
                            paramField.add(null);
                            paramClassList.add(String.class);
                            staticParam.add(updateParam.substring(1, updateParam.length() - 1));
                            continue;
                        }
                        if(NumberUtils.isNumber(updateParam)) {
                            if(updateParam.contains(".")) {
                                paramField.add(null);
                                paramClassList.add(BigDecimal.class);
                                staticParam.add(new BigDecimal(updateParam));
                            } else {
                                paramField.add(null);
                                paramClassList.add(Integer.class);
                                staticParam.add(Integer.valueOf(updateParam));
                            }
                            continue;
                        }
                        if("TRUE".equals(updateParam.toUpperCase())) {
                            paramField.add(null);
                            paramClassList.add(Boolean.class);
                            staticParam.add(true);
                            continue;
                        }
                        if("FALSE".equals(updateParam.toUpperCase())) {
                            paramField.add(null);
                            paramClassList.add(Boolean.class);
                            staticParam.add(false);
                            continue;
                        }
                        DtoField dtoField = dtoClassInfo.getDtoField(updateParam);
                        if (dtoField != null) {
                            paramField.add(dtoField);
                            paramClassList.add(dtoField.getOwnClass());
                            staticParam.add(null);
                            continue;
                        }
                        throw new MybatisAccessorException(MybatisAccessorException.ExceptionType.METHOD_INVOKE_EXCEPTION, "参数不正确:" + updateParam);
                    } else {
                        paramField.add(null);
                        paramClassList.add(String.class);
                        staticParam.add(updateParam);
                    }
                }
            }

            Method method;
            if (!paramClassList.isEmpty()) {
                method = bean.getClass().getDeclaredMethod(methodName, paramClassList.toArray(new Class[0]));
            } else {
                method = bean.getClass().getDeclaredMethod(methodName, dtoClassInfo.getDtoClass());
            }
            List<Object> methodParams = new ArrayList<>();
            for (Object dto : dtoList) {
                methodParams.clear();
                if (!paramField.isEmpty()) {
                    for (int i = 0 ; i < paramField.size(); i ++) {
                        if(paramField.get(i) != null) {
                            methodParams.add(FieldUtils.getFieldValue(dto, paramField.get(i).getPropertyDescriptor().getReadMethod()));
                        } else {
                            methodParams.add(staticParam.get(i));
                        }
                    }
                } else {
                    methodParams.add(dto);
                }
                method.invoke(bean, methodParams.toArray());
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException ex) {
            throw new MybatisAccessorException(MybatisAccessorException.ExceptionType.METHOD_INVOKE_EXCEPTION, ex);
        }
    }
}
