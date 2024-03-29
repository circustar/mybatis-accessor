package com.circustar.mybatis_accessor.listener.event.decode;

import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IDecodeEvent;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.service.ISelectService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultDecodeEvent implements IDecodeEvent<DecodeEventModel> {
    private static DefaultDecodeEvent decodeEvent = new DefaultDecodeEvent();
    public static DefaultDecodeEvent getInstance() {
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
            , String updateEventLogId) throws MybatisAccessorException {
        DtoClassInfoHelper dtoClassInfoHelper = dtoClassInfo.getDtoClassInfoHelper();
        DtoClassInfo sourceDtoClassInfo = model.getSourceDtoClassInfo(dtoClassInfoHelper);
        ISelectService selectService = sourceDtoClassInfo.getDtoClassInfoHelper().getSelectService();
        List<DtoField> sourcePropertyDtoFieldList = model.getSourcePropertyDtoFieldList();
        List<DtoField> targetPropertyDtoFieldList = model.getTargetPropertyDtoFieldList();
        List<DtoField> matchProperties = model.getMatchPropertyDtoFields();
        List matchValues = new ArrayList();
        Object queryDto = sourceDtoClassInfo.createInstance();
        for (Object dto : dtoList) {
            matchValues.clear();
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
                    throw new MybatisAccessorException(MybatisAccessorException.ExceptionType.TARGET_NOT_FOUND
                            , "Decode event failed because source is not found. class : " + sourceDtoClassInfo.getDtoClass().getSimpleName()
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
    }
}
