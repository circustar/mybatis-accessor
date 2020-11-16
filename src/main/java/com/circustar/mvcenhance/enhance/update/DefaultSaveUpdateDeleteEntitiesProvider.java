package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.utils.ArrayParamUtils;

import java.util.*;

public class DefaultSaveUpdateDeleteEntitiesProvider extends AutoDetectUpdateEntityProvider {
    @Override
    public String defineUpdateName() {
        return IUpdateEntityProvider.SAVE_UPDATE_DELETE_LIST;
    }

    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation, Object object, Object... options) throws Exception {
//        if(s == null || !Collection.class.isAssignableFrom(s.getClass())) {
//            return null;
//        }
//        boolean isPhysic = false;
//        if(options != null && options.length > 0) {
//            isPhysic = (boolean) options[0];
//        }
//        UpdateEntity updateEntity = new UpdateEntity(relation, UpdateCommand.SAVE_OR_UPDATE
//                , applicationContext.getBean(relation.getService()));
//        UpdateEntity deleteEntity = new UpdateEntity(relation
//                , isPhysic?UpdateCommand.PHYSIC_DELETE_ID : UpdateCommand.DELETE_ID
//                , applicationContext.getBean(relation.getService()));
//
//        String deleteFieldName = AnnotationUtils.getDeleteFieldAnnotationValue(relation.getDto());
//        for(Object o : (Collection) s) {
//            Object entity = getConversionService().convert(o, relation.getEntity());
//            if(!StringUtils.isEmpty(deleteFieldName)) {
//                Object deleteValue = FieldUtils.getValueByName(o, deleteFieldName);
//                if(Objects.nonNull(deleteValue)) {
//                    deleteEntity.addObject(entity);
//                    continue;
//                }
//            }
//            updateEntity.addObject(entity);
//        }
//        List<UpdateEntity> updateEntities = new ArrayList<>();
//        if(deleteEntity.getObjList() != null) {
//            updateEntities.add(deleteEntity);
//        }
//        if(updateEntity.getObjList() != null) {
//            updateEntities.add(updateEntity);
//        }
//        return updateEntities;

        UpdateSubEntityStrategy updateSubEntityStrategy = ArrayParamUtils.parseArray(options, 0, UpdateSubEntityStrategy.DELETE_BEFORE_INSERT);
        boolean physicDelete = ArrayParamUtils.parseArray(options, 1, false);

        List<UpdateEntity> result = new ArrayList<>();
        Collection c = (Collection) object;
        for(Object o : c) {
            result.add(super.createUpdateEntity(relation, o
                    , updateSubEntityStrategy
                    , physicDelete, true, false));
        }
        return result;
    }

}
