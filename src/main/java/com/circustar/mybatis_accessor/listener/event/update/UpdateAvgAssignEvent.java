package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.classInfo.DtoField;

import java.math.BigDecimal;
import java.util.List;

public class UpdateAvgAssignEvent extends UpdateAssignEvent implements IUpdateEvent<UpdateEventModel> {
    @Override
    protected BigDecimal getTotalWeight(List sEntityList, DtoField sWeightDtoField) {
        return BigDecimal.valueOf(sEntityList.size());
    }

    @Override
    protected BigDecimal getNextWeight(Object sEntity, DtoField sWeightDtoField) {
        return BigDecimal.ONE;
    }

    @Override
    protected DtoField getWeightEntityField(List<DtoField> dtoFields) {
        return null;
    }
}
