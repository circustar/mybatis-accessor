package com.circustar.mybatis_accessor.listener.event.decode;

import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IDecodeEvent;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.service.ISelectService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class DefaultDecodeEvent implements IDecodeEvent<DecodeEventModel> {
    private static DefaultDecodeEvent decodeEvent;
    private static Lock lock = new ReentrantLock();
    public static DefaultDecodeEvent getInstance() {
        if (decodeEvent != null) {
            return decodeEvent;
        }
        if(lock.tryLock()) {
            try {
                decodeEvent = new DefaultDecodeEvent();
            } finally {
                lock.unlock();
            }
        }
        return decodeEvent;
    }

    @Override
    public ExecuteTiming getDefaultExecuteTiming() {
        return ExecuteTiming.BEFORE_ENTITY_UPDATE;
    }

    @Override
    public IUpdateCommand.UpdateType[] getDefaultUpdateTypes() {
        return new IUpdateCommand.UpdateType[] {IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE};
    }

    @Override
    public void exec(DecodeEventModel model, IUpdateCommand.UpdateType updateType
            , DtoClassInfo dtoClassInfo, List<Object> dtoList
            , String updateEventLogId, int level) {
        DtoClassInfoHelper dtoClassInfoHelper = dtoClassInfo.getDtoClassInfoHelper();
        DtoClassInfo sourceDtoClassInfo = model.getSourceDtoClassInfo(dtoClassInfoHelper);
        ISelectService selectService = sourceDtoClassInfo.getDtoClassInfoHelper().getSelectService();
        List<DtoField> sourcePropertyDtoFieldList = model.getSourcePropertyDtoFieldList();
        List<DtoField> targetPropertyDtoFieldList = model.getTargetPropertyDtoFieldList();
        List<DtoField> matchProperties = model.getMatchPropertyDtoFields();
        try {
            Object queryDto = sourceDtoClassInfo.createInstance();
            for (Object dto : dtoList) {
                List matchValues = new ArrayList();
                for (int i = 0; i < matchProperties.size(); i++) {
                    Object fieldValue = FieldUtils.getFieldValue(dto, matchProperties.get(i).getPropertyDescriptor().getReadMethod());
                    FieldUtils.setFieldValue(queryDto
                            , model.getMatchSourcePropertyDtoFields().get(i).getPropertyDescriptor().getWriteMethod(), fieldValue);
                    matchValues.add(fieldValue);
                }
                Object sourceDto = selectService.getDtoByAnnotation(sourceDtoClassInfo.getEntityDtoServiceRelation()
                        , queryDto, false, null);
                if(sourceDto == null) {
                    if (model.isErrorWhenNotExist()) {
                        throw new Exception("Decode event failed because source is not found. class : " + sourceDtoClassInfo.getDtoClass().getSimpleName()
                                + " - match properties : " + matchProperties.stream().map(x -> x.getField().getName()).collect(Collectors.joining(","))
                                + " - match values : " + matchValues.stream().collect(Collectors.joining(",")));
                    } else {
                        continue;
                    }
                }

                for(int i = 0; i < sourcePropertyDtoFieldList.size(); i++) {
                    Object resultValue = FieldUtils.getFieldValue(sourceDto, sourcePropertyDtoFieldList.get(i).getPropertyDescriptor().getReadMethod());
                    FieldUtils.setFieldValue(dto
                            , targetPropertyDtoFieldList.get(i).getPropertyDescriptor().getWriteMethod(), resultValue);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
