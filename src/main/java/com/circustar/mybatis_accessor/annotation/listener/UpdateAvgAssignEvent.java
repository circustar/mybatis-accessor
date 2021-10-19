package com.circustar.mybatis_accessor.annotation.listener;

import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;

import java.math.BigDecimal;
import java.util.List;

public class UpdateAvgAssignEvent extends UpdateAssignEvent implements IUpdateEvent {
    @Override
    protected BigDecimal getTotalWeight(List sEntityList, EntityFieldInfo sWeightEntityField) {
        return BigDecimal.valueOf(sEntityList.size());
    }

    @Override
    protected BigDecimal getNextWeight(Object sEntity, EntityFieldInfo sWeightEntityField) {
        return BigDecimal.ONE;
    }

    @Override
    protected EntityFieldInfo getWeightEntityField(List<DtoField> dtoFields) {
        return null;
    }
}
