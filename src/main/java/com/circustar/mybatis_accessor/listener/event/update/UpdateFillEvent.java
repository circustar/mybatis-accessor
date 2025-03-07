package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.dto.QueryOrder;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.support.MybatisAccessorService;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class UpdateFillEvent extends AbstractUpdateEvent<UpdateEventModel> implements IUpdateEvent<UpdateEventModel> {
    protected MybatisAccessorService mybatisAccessorService;
    public UpdateFillEvent(MybatisAccessorService mybatisAccessorService) {
        super();
        this.mybatisAccessorService = mybatisAccessorService;
    }

    @Override
    public IUpdateCommand.UpdateType[] getDefaultUpdateTypes() {
        return new IUpdateCommand.UpdateType[]{
                IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE
        };
    }

    @Override
    public ExecuteTiming getDefaultExecuteTiming() {
        return ExecuteTiming.AFTER_SUB_ENTITY_UPDATE;
    }

    @Override
    protected List<DtoField> parseDtoFieldList(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo) {
        DtoField mAssignField = dtoClassInfo.getDtoField(updateEventModel.getUpdateParams().get(0));
        DtoField mRemainField = dtoClassInfo.getDtoField(updateEventModel.getUpdateParams().get(1));
        DtoField sField = dtoClassInfo.getDtoField(updateEventModel.getUpdateParams().get(2));
        DtoField sFillField = sField.getFieldDtoClassInfo().getDtoField(updateEventModel.getUpdateParams().get(3));
        DtoField sLimitField = null;
        if(!NumberUtils.isNumber(updateEventModel.getUpdateParams().get(4))) {
            sLimitField = sField.getFieldDtoClassInfo().getDtoField(updateEventModel.getUpdateParams().get(4));
        }
        DtoField sOrderField = null;
        if(StringUtils.hasLength(updateEventModel.getUpdateParams().get(5))) {
            sOrderField = sField.getFieldDtoClassInfo().getDtoField(updateEventModel.getUpdateParams().get(5));
        }

        return Arrays.asList(mAssignField, mRemainField, sField, sFillField, sLimitField, sOrderField);
    }

    @Override
    protected List<Object> parseParams(UpdateEventModel updateEventModel, List<DtoField> dtoFields, DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo) {
        boolean isAsc = true;
        if(updateEventModel.getUpdateParams().size() > 6
                && QueryOrder.ORDER_DESC.equals(updateEventModel.getUpdateParams().get(6).toLowerCase(Locale.getDefault()))) {
            isAsc = false;
        }
        BigDecimal limitValue = BigDecimal.ZERO;
        if(NumberUtils.isNumber(updateEventModel.getUpdateParams().get(4))) {
            limitValue = BigDecimal.valueOf(Double.valueOf(updateEventModel.getUpdateParams().get(4)));
        }
        return Arrays.asList(isAsc, limitValue);
    }

    @Override
    protected void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo
            , List<Object> dtoList, List<DtoField> dtoFields, List<Object> parsedParams
            , String updateEventLogId) throws MybatisAccessorException {
        DtoField mAssignField = dtoFields.get(0);
        DtoField mRemainField = dtoFields.get(1);
        DtoField sField = dtoFields.get(2);
        String sFieldName = sField.getField().getName();
        DtoField sFillField = dtoFields.get(3);
        DtoField sLimitField = dtoFields.get(4);
        DtoField sOrderField = dtoFields.get(5);

        Method keyFieldReadMethod = dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();
        ISelectService selectService = dtoClassInfo.getDtoClassInfoHelper().getSelectService();
        boolean isAsc = (boolean) parsedParams.get(0);
        BigDecimal paramLimitValue = (BigDecimal) parsedParams.get(1);
        List updateSubDtoList = new ArrayList();
        List updateDtoList = new ArrayList();
        for(Object dto : dtoList) {
            Serializable keyValue = (Serializable)FieldUtils.getFieldValue(dto, keyFieldReadMethod);
            Object dtoById = selectService.getDtoById(dtoClassInfo.getEntityDtoServiceRelation(), keyValue, false
                    , Collections.singletonList(sFieldName));
            BigDecimal remainFillValue = NumberUtils.readDecimalValue(dtoById, mAssignField.getPropertyDescriptor().getReadMethod());
            if(remainFillValue.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            List fieldValueList = CollectionUtils.convertToList(FieldUtils.getFieldValue(dtoById
                    , sField.getPropertyDescriptor().getReadMethod()));
            if(sOrderField != null) {
                fieldValueList = (List) fieldValueList.stream().sorted((x, y) -> {
                    Comparable orderValueX = (Comparable) FieldUtils.getFieldValue(x, sOrderField.getPropertyDescriptor().getReadMethod());
                    if (orderValueX == null) {
                        return 1;
                    }
                    Comparable orderValueY = (Comparable) FieldUtils.getFieldValue(y, sOrderField.getPropertyDescriptor().getReadMethod());
                    if (orderValueY == null) {
                        return -1;
                    }
                    return isAsc?orderValueX.compareTo(orderValueY):orderValueY.compareTo(orderValueX);
                }).collect(Collectors.toList());
            }
            for(Object fieldValue : fieldValueList) {
                BigDecimal filledValue = NumberUtils.readDecimalValue(fieldValue, sFillField.getPropertyDescriptor().getReadMethod());
                BigDecimal limitValue = sLimitField == null ? paramLimitValue
                        : NumberUtils.readDecimalValue(fieldValue, sLimitField.getPropertyDescriptor().getReadMethod());
                BigDecimal lackValue = limitValue.subtract(filledValue);
                if(lackValue.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                BigDecimal toFillValue;
                if(remainFillValue.compareTo(lackValue) <= 0) {
                    toFillValue = remainFillValue;
                    remainFillValue = BigDecimal.ZERO;
                } else {
                    toFillValue = lackValue;
                    remainFillValue = remainFillValue.subtract(lackValue);
                }
                Object resultValue = NumberUtils.castFromBigDecimal(sFillField.getActualClass(), filledValue.add(toFillValue));
                FieldUtils.setFieldValue(fieldValue, sFillField.getPropertyDescriptor().getWriteMethod(), resultValue);
                updateSubDtoList.add(fieldValue);
            }
            Object oldRemainObjectValue = FieldUtils.getFieldValue(dtoById, mRemainField.getPropertyDescriptor().getReadMethod());
            if(oldRemainObjectValue != null && remainFillValue.compareTo(NumberUtils.castToBigDecimal(oldRemainObjectValue)) == 0) {
                continue;
            }
            Object remainObjectValue = NumberUtils.castFromBigDecimal(mAssignField.getActualClass(), remainFillValue);
            FieldUtils.setFieldValue(dto, mRemainField.getPropertyDescriptor().getWriteMethod(), remainObjectValue);
            if (mRemainField.getEntityFieldInfo() != null
                    && (mRemainField.getEntityFieldInfo().getTableField() == null
                    || mRemainField.getEntityFieldInfo().getTableField().exist())) {
                FieldUtils.setFieldValue(dtoById, mRemainField.getPropertyDescriptor().getWriteMethod(), remainObjectValue);
                updateDtoList.add(dtoById);
            }
        }
        if(!updateSubDtoList.isEmpty()) {
            mybatisAccessorService.updateList(fieldDtoClassInfo.getEntityDtoServiceRelation(), updateSubDtoList
                    , Collections.EMPTY_LIST, false, updateEventLogId);
        }
        if(!updateDtoList.isEmpty()) {
            mybatisAccessorService.updateList(dtoClassInfo.getEntityDtoServiceRelation(), updateDtoList
                    , Collections.EMPTY_LIST, false, updateEventLogId);
        }
    }

    @Override
    protected DtoClassInfo getFieldDtoClassInfo(List<DtoField> dtoFields) {
        return dtoFields.get(2).getFieldDtoClassInfo();
    }
}
