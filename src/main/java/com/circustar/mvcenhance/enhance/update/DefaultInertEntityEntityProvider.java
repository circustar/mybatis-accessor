package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultInertEntityEntityProvider extends AutoDetectUpdateEntityProvider {
    @Override
    public String defineUpdateName() {
        return IUpdateEntityProvider.INSERT;
    }

    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object object, Map options) throws Exception {

        List<UpdateEntity> result = new ArrayList<>();
        result.add(super.createUpdateEntity(relation, dtoClassInfoHelper, object
                , UpdateSubEntityStrategy.INSERT_OR_UPDATE
                , false, true, false));
        return result;
    };

}
