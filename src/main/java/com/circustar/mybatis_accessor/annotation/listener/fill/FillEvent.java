package com.circustar.mybatis_accessor.annotation.listener.fill;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.dto.QueryOrder;
import com.circustar.mybatis_accessor.annotation.listener.AbstractUpdateEvent;
import com.circustar.mybatis_accessor.annotation.listener.IUpdateEvent;
import com.circustar.mybatis_accessor.annotation.listener.UpdateEventModel;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.service.ISelectService;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FillEvent extends AbstractUpdateEvent<UpdateEventModel> implements IUpdateEvent<UpdateEventModel> {
    @Override
    public IUpdateCommand.UpdateType[] getUpdateTypes() {
        return new IUpdateCommand.UpdateType[]{
                IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE
        };
    }

    @Override
    protected List<DtoField> parseDtoFieldList(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo) {
        DtoField mAssignField = dtoClassInfo.getDtoField(updateEventModel.getUpdateParams()[0]);
        DtoField mRemainField = dtoClassInfo.getDtoField(updateEventModel.getUpdateParams()[1]);
        DtoField sField = dtoClassInfo.getDtoField(updateEventModel.getUpdateParams()[2]);
        DtoField sFillField = sField.getFieldDtoClassInfo().getDtoField(updateEventModel.getUpdateParams()[3]);
        DtoField sLimitField = null;
        if(!NumberUtils.isNumber(updateEventModel.getUpdateParams()[4])) {
            sLimitField = sField.getFieldDtoClassInfo().getDtoField(updateEventModel.getUpdateParams()[4]);
        }
        DtoField sOrderField = null;
        if(StringUtils.hasLength(updateEventModel.getUpdateParams()[5])) {
            sOrderField = sField.getFieldDtoClassInfo().getDtoField(updateEventModel.getUpdateParams()[5]);
        }

        return Arrays.asList(mAssignField, mRemainField, sField, sFillField, sLimitField, sOrderField);
    }

    @Override
    protected List<Object> parseParams(UpdateEventModel updateEventModel, List<DtoField> dtoFields, DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo) {
        boolean isAsc = true;
        if(updateEventModel.getUpdateParams().length > 6) {
            if(QueryOrder.ORDER_DESC.equals(updateEventModel.getUpdateParams()[6].toLowerCase())) {
                isAsc = false;
            }
        }
        BigDecimal limitValue = BigDecimal.ZERO;
        if(NumberUtils.isNumber(updateEventModel.getUpdateParams()[4])) {
            limitValue = BigDecimal.valueOf(Double.valueOf(updateEventModel.getUpdateParams()[4]));
        }
        return Arrays.asList(isAsc, limitValue);
    }

    @Override
    protected void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo
            , List<Object> dtoList, List<Object> entityList, List<DtoField> dtoFields, List<Object> parsedParams) {
        DtoField mAssignField = dtoFields.get(0);
        DtoField mRemainField = dtoFields.get(1);
        DtoField sField = dtoFields.get(2);
        String sFieldName = sField.getField().getName();
        DtoField sFillField = dtoFields.get(3);
        DtoField sLimitField = dtoFields.get(4);
        DtoField sOrderField = dtoFields.get(5);

        Method keyFieldReadMethod = dtoClassInfo.getEntityClassInfo().getKeyField().getPropertyDescriptor().getReadMethod();
        Method sKeyFieldReadMethod = fieldDtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();
        ISelectService selectService = dtoClassInfo.getDtoClassInfoHelper().getSelectService();
        IService serviceBean = fieldDtoClassInfo.getServiceBean();
        boolean isAsc = (boolean) parsedParams.get(0);
        BigDecimal paramLimitValue = (BigDecimal) parsedParams.get(1);
        for(int i = 0; i< entityList.size(); i++) {
            Serializable keyValue = (Serializable)FieldUtils.getFieldValue(entityList.get(i), keyFieldReadMethod);
            Object dtoById = selectService.getDtoById(dtoClassInfo.getEntityDtoServiceRelation(), keyValue, false
                    , new String[]{sFieldName});
            BigDecimal remainFillValue = NumberUtils.readDecimalValue(mAssignField.getActualClass(), dtoById, mAssignField.getPropertyDescriptor().getReadMethod());
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
                BigDecimal filledValue = NumberUtils.readDecimalValue(sFillField.getActualClass(), fieldValue, sFillField.getPropertyDescriptor().getReadMethod());
                BigDecimal limitValue = sLimitField == null ? paramLimitValue
                        : NumberUtils.readDecimalValue(sLimitField.getActualClass(), fieldValue, sLimitField.getPropertyDescriptor().getReadMethod());
                BigDecimal lackValue = limitValue.subtract(filledValue);
                if(lackValue.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                Object subId = FieldUtils.getFieldValue(fieldValue, sKeyFieldReadMethod);
                BigDecimal toFillValue;
                if(remainFillValue.compareTo(lackValue) <= 0) {
                    toFillValue = remainFillValue;
                    remainFillValue = BigDecimal.ZERO;
                } else {
                    toFillValue = lackValue;
                    remainFillValue = remainFillValue.subtract(lackValue);
                }
                Object resultValue = NumberUtils.castFromBigDecimal(sFillField.getActualClass(), filledValue.add(toFillValue));

                UpdateWrapper uw = new UpdateWrapper();
                uw.set(sFillField.getEntityFieldInfo().getColumnName(), resultValue);
                uw.eq(fieldDtoClassInfo.getEntityClassInfo().getKeyField().getColumnName(), subId);
                serviceBean.update(uw);
            }
            Object remainObjectValue = NumberUtils.castFromBigDecimal(mAssignField.getActualClass(), remainFillValue);
            FieldUtils.setFieldValue(dtoList.get(i), mRemainField.getPropertyDescriptor().getWriteMethod(), remainObjectValue);
        }
    }

    @Override
    protected DtoClassInfo getFieldDtoClassInfo(List<DtoField> dtoFields) {
        return dtoFields.get(2).getFieldDtoClassInfo();
    }
}
