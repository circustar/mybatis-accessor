package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.MvcEnhanceConstants;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.utils.MapOptionUtils;

import java.util.*;

public class DefaultUpdateSubEntitiesProvider extends AutoDetectUpdateEntityProvider {
    @Override
    public String defineUpdateName() {
        return IUpdateEntityProvider.UPDATE_SUB_ENTITIES;
    }

    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object object, Map options) throws Exception {

        UpdateSubEntityStrategy updateSubEntityStrategy = MapOptionUtils.getValue(options, UpdateSubEntityStrategy.class, UpdateSubEntityStrategy.DELETE_BEFORE_INSERT);
        Boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, true);

        List<UpdateEntity> result = new ArrayList<>();
        result.add(super.createUpdateEntity(relation, dtoClassInfoHelper
                , object
                , updateSubEntityStrategy
                , physicDelete
                , false
                , false));
        return result;
    };
}
