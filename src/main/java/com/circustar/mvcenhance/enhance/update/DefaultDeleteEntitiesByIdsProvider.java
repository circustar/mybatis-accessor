package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.utils.ArrayParamUtils;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultDeleteEntitiesByIdsProvider extends AutoDetectUpdateEntityProvider {
    @Override
    public String defineUpdateName() {
        return IUpdateEntityProvider.DELETE_LIST_BY_IDS;
    }

    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object ids, Object... options) throws Exception {
        if(ids == null) {
            return null;
        }
        boolean isPhysic = ArrayParamUtils.parseArray(options, 0, false);
        UpdateEntity updateEntity = new UpdateEntity(relation
                , isPhysic? UpdateCommand.PHYSIC_DELETE_ID: UpdateCommand.DELETE_ID
                , applicationContext.getBean(relation.getService()));

        updateEntity.setObjList((ids instanceof List)?(List)ids : Arrays.asList(ids));
        return Collections.singletonList(updateEntity);
    };

    @Override
    public void validateAndSet(Object obj, BindingResult bindingResult, Object... options){
    };
}
