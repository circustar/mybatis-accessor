package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.MvcEnhanceConstants;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.utils.MapOptionUtils;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DefaultDeleteEntitiesByIdsProvider extends AutoDetectUpdateEntityProvider {
    @Override
    public String defineUpdateName() {
        return IUpdateEntityProvider.DELETE_LIST_BY_IDS;
    }

    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object ids, Map options) throws Exception {
        if(ids == null) {
            return null;
        }
        Boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, true);
        UpdateEntity updateEntity = new UpdateEntity(relation
                , physicDelete? UpdateCommand.PHYSIC_DELETE_ID: UpdateCommand.DELETE_ID
                , applicationContext.getBean(relation.getService()));

        updateEntity.setObjList((ids instanceof List)?(List)ids : Arrays.asList(ids));
        return Collections.singletonList(updateEntity);
    };

    @Override
    public void validateAndSet(Object obj, BindingResult bindingResult, Map options){
    };
}
