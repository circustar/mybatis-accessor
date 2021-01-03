package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;

import java.util.ArrayList;
import java.util.List;

public class DefaultInertEntityEntityProvider extends AutoDetectUpdateEntityProvider {
    @Override
    public String defineUpdateName() {
        return IUpdateEntityProvider.INSERT;
    }

    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object object, Object... options) throws Exception {
//        if(s == null) {
//            return null;
//        }
//
//        Object entity = getConversionService().convert(s, relation.getEntity());
//        UpdateEntity updateEntity = new UpdateEntity(relation, UpdateCommand.INSERT
//                , applicationContext.getBean(relation.getService()), false);
//        updateEntity.setRelation(relation);
//        updateEntity.setUpdateCommand(UpdateCommand.INSERT);
//        updateEntity.setObjList(Collections.singletonList(entity));
//
//        String[] subEntities = null;
//        if(options != null && options.length > 0) {
//            if(options[0].getClass().isArray()) {
//                subEntities = (String[])options[0];
//            } else {
//                subEntities = options[0].toString().replace(" ", "").split(",");
//            }
//        }
//
//        List<UpdateEntity> updateEntityList = Collections.singletonList(updateEntity);
//        if(subEntities == null) {
//            return updateEntityList;
//        }
//        //TODO: Cache
//        DtoClassInfo dtoClassInfo = new DtoClassInfo(relation.getDto(), relation.getEntity());
//        // TODO: 解决嵌套
//        for(String subEntityName : subEntities) {
//            DtoField dtoField = dtoClassInfo.findDtoField(subEntityName);
//            if(dtoField == null) {
//                continue;
//            }
//            EntityDtoServiceRelation subEntityRelation = getRelationMap().getByDtoClass((Class)dtoField.getActualType());
//            if(subEntityRelation == null) {
//                continue;
//            }
//            UpdateEntity subUpdateEntity = new UpdateEntity(subEntityRelation, UpdateCommand.INSERT
//                    , applicationContext.getBean(subEntityRelation.getService()), false);
//            Object value = dtoField.getValue(s);
//
//            List<Object> objList = new ArrayList<>();
//            if(dtoField.isCollection()) {
//                for(Object v : (Collection)value) {
//                    Object subEntity = getConversionService().convert(v, subEntityRelation.getEntity());
//                    objList.add(subEntity);
//                }
//            } else {
//                Object subEntity = getConversionService().convert(value, subEntityRelation.getEntity());
//                objList.add(subEntity);
//            }
//            subUpdateEntity.setObjList(objList);
//            updateEntity.addSubUpdateEntity(subUpdateEntity);
//        }
//        return updateEntityList;
        List<UpdateEntity> result = new ArrayList<>();
        result.add(super.createUpdateEntity(relation, dtoClassInfoHelper, object
                , UpdateSubEntityStrategy.INSERT_OR_UPDATE
                , false, true, false));
        return result;
    };

}
