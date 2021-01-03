package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultInertEntitiesEntityProvider extends AutoDetectUpdateEntityProvider {
    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object object, Object... options) throws Exception {
        if(object == null || !Collection.class.isAssignableFrom(object.getClass())) {
            return null;
        }
//        UpdateEntity updateEntity = null;
//        for(Object o : (Collection) s) {
//            if(relation == null) {
//                relation = getRelationMap().getByDtoClass(o.getClass());
//                updateEntity = new UpdateEntity(relation, UpdateCommand.INSERT
//                        , applicationContext.getBean(relation.getService()), false);
//                updateEntity.setObjList(new ArrayList<>());
//            }
//            Object entity = getConversionService().convert(s, relation.getEntity());
//            updateEntity.getObjList().add(entity);
//        }
//        return Collections.singletonList(updateEntity);

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
