package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DefaultInertEntitiesEntityProvider extends AutoDetectUpdateEntityProvider {
    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object object, Map options) throws Exception {
        if(object == null || !Collection.class.isAssignableFrom(object.getClass())) {
            return null;
        }

        List<UpdateEntity> result = new ArrayList<>();
        Collection c = (Collection) object;
        for(Object o : c) {
            result.add(super.createUpdateEntity(relation, dtoClassInfoHelper, o
                    , UpdateSubEntityStrategy.INSERT_OR_UPDATE
                    , false, true, false));
        }
        return result;
    };

}
