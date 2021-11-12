package com.circustar.mybatis_accessor.listener.event.decode;

import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IDecodeEvent;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.service.ISelectService;

import java.util.List;

public class DefaultDecodeEvent implements IDecodeEvent<DecodeEventModel> {
    private static DefaultDecodeEvent defaultDecodeEvent = null;
    public static DefaultDecodeEvent getInstance() {
        if (defaultDecodeEvent == null) {
            defaultDecodeEvent = new DefaultDecodeEvent();
        }
        return defaultDecodeEvent;
    }

    @Override
    public void exec(DecodeEventModel model, IUpdateCommand.UpdateType updateType
            , DtoClassInfo dtoClassInfo, List<Object> dtoList, List<Object> entityList) {
        DtoClassInfoHelper dtoClassInfoHelper = dtoClassInfo.getDtoClassInfoHelper();
        DtoClassInfo sourceDtoClassInfo = model.getSourceDtoClassInfo(dtoClassInfoHelper);
        ISelectService selectService = sourceDtoClassInfo.getDtoClassInfoHelper().getSelectService();
        try {
            for (Object dto : dtoList) {
                Object sourceQueryDto = sourceDtoClassInfo.getDtoClass().newInstance();
                List<DtoField> matchProperties = model.getMatchPropertyDtoFields();
                for (int i = 0; i < matchProperties.size(); i++) {
                    Object fieldValue = FieldUtils.getFieldValue(dto, matchProperties.get(i).getPropertyDescriptor().getReadMethod());
                    FieldUtils.setFieldValue(sourceQueryDto
                            , model.getMatchSourcePropertyDtoFields().get(i).getPropertyDescriptor().getWriteMethod(), fieldValue);
                }
                Object sourceDto = selectService.getDtoByAnnotation(sourceDtoClassInfo.getEntityDtoServiceRelation(), sourceQueryDto, false, null);
                if (model.isErrorWhenNotExist() && sourceDto == null) {
                    throw new Exception("decode event failed");
                }
                Object resultValue = FieldUtils.getFieldValue(sourceDto, model.getSourcePropertyDtoField().getPropertyDescriptor().getReadMethod());
                FieldUtils.setFieldValue(dto
                        , model.getTargetPropertyDtoField().getPropertyDescriptor().getWriteMethod(), resultValue);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Class " + dtoClassInfo.getDtoClass().getSimpleName() + " decode event failed");
        }
    }
}
