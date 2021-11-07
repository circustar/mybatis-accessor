package com.circustar.mybatis_accessor.listener.event.update_event;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.collection.NumberUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.listener.event.IUpdateEvent;
import com.circustar.mybatis_accessor.listener.event.UpdateEventModel;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;
import com.circustar.mybatis_accessor.service.ISelectService;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UpdateCountEvent extends UpdateCountSqlEvent implements IUpdateEvent<UpdateEventModel> {

    @Override
    protected List<Object> parseParams(UpdateEventModel updateEventModel, List<DtoField> dtoFields
            , DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo) {
        return Arrays.asList(updateEventModel.getUpdateParams());
    }

    protected BigDecimal getValue(Object dtoUpdated, List<DtoField> dtoFields, List<Object> parsedParams) {
        Object fieldValue = FieldUtils.getFieldValue(dtoUpdated, dtoFields.get(1).getPropertyDescriptor().getReadMethod());
        int count = 0;
        if(fieldValue != null) {
            count = CollectionUtils.convertToList(fieldValue).size();
        }
        return BigDecimal.valueOf(count);
    }

    @Override
    protected void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo
            , List<Object> dtoList, List<Object> entityList, List<DtoField> dtoFields, List<Object> parsedParams) {
        IService serviceBean = dtoClassInfo.getServiceBean();
        ISelectService selectService = dtoClassInfo.getDtoClassInfoHelper().getSelectService();
        DtoField mField = dtoFields.get(0);
        DtoField sField = dtoFields.get(1);
        EntityFieldInfo mKeyField = dtoClassInfo.getEntityClassInfo().getKeyField();
        Method mKeyFieldReadMethod = mKeyField.getPropertyDescriptor().getReadMethod();

        for(int i = 0; i< entityList.size(); i++) {
            Serializable mKeyValue = (Serializable) FieldUtils.getFieldValue(entityList.get(i), mKeyFieldReadMethod);
            Object dtoUpdated = selectService.getDtoById(dtoClassInfo.getEntityDtoServiceRelation(), mKeyValue
                    , false, Collections.singletonList(sField.getField().getName()));
            Object count = NumberUtils.castFromBigDecimal(mField.getActualClass()
                    , getValue(dtoUpdated, dtoFields, parsedParams)) ;

            UpdateWrapper uw = new UpdateWrapper();
            uw.set(mField.getEntityFieldInfo().getColumnName(), count);
            uw.eq(mKeyField.getColumnName(), mKeyValue);
            serviceBean.update(uw);
        }
    }
}
