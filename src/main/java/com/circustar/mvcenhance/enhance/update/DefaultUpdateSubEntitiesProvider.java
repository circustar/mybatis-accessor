package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.utils.ArrayParamUtils;

import java.util.*;

public class DefaultUpdateSubEntitiesProvider extends AutoDetectUpdateEntityProvider {
    @Override
    public String defineUpdateName() {
        return IUpdateEntityProvider.UPDATE_SUB_ENTITIES;
    }

    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation, Object object, Object... options) throws Exception {
//        if(updateObject == null) {
//            return null;
//        }
//        TableInfo tableInfo = TableInfoHelper.getTableInfo(relation.getEntity());
//        Object entity = getConversionService().convert(updateObject, relation.getEntity());
//        UpdateEntity updateEntity = new UpdateEntity(relation, UpdateCommand.UPDATE_ID
//                , applicationContext.getBean(relation.getService()), false);
////        updateEntity.setObjList(Collections.singletonList(entity));
//
//        String[] subEntities = null;
//        UpdateSubEntityStrategy updateSubEntityStrategy = UpdateSubEntityStrategy.INSERT_OR_UPDATE;
//        boolean physicDelete = false;
//        if(options!= null && options.length > 0) {
//            if(options[0] != null) {
//                if(options[0].getClass().isArray()) {
//                    subEntities = (String[])options[0];
//                } else {
//                    subEntities = options[0].toString().replace(" ", "").split(",");
//                }
//            }
//            if(options.length > 1 && options != null) {
//                updateSubEntityStrategy = (UpdateSubEntityStrategy)options[1];
//            }
//            if(options.length > 2 && options != null) {
//                physicDelete = (boolean)options[2];
//            }
//        }
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
//
//            IService subService = applicationContext.getBean(subEntityRelation.getService());
//            UpdateEntity subUpdateEntity = null;
//            UpdateEntity deleteSubEntity = new UpdateEntity(subEntityRelation
//                    , physicDelete? UpdateCommand.PHYSIC_DELETE_ID : UpdateCommand.DELETE_ID
//                    , subService);
//
//            if(updateSubEntityStrategy == UpdateSubEntityStrategy.DELETE_BEFORE_INSERT) {
//                subUpdateEntity = new UpdateEntity(subEntityRelation, UpdateCommand.INSERT, subService);
//            } else {
//                subUpdateEntity = new UpdateEntity(subEntityRelation, UpdateCommand.SAVE_OR_UPDATE, subService);
//            }
//            Object values = dtoField.getValue(updateObject);
//
//            String deleteFieldName = AnnotationUtils.getDeleteFieldAnnotationValue(subEntityRelation.getDto());
//
//            if(dtoField.isCollection()) {
//                for(Object v : (Collection)values) {
//                    Object subEntity = getConversionService().convert(v, subEntityRelation.getEntity());
//                    if(!StringUtils.isEmpty(deleteFieldName)) {
//                        Object deleteValue = FieldUtils.getValueByName(v, deleteFieldName);
//                        if(Objects.nonNull(deleteValue)) {
//                            deleteSubEntity.addObject(subEntity);
//                            continue;
//                        }
//                    }
//                    Object masterKey = FieldUtils.getValueByName(entity, tableInfo.getKeyProperty());
//                    FieldUtils.setFieldByName(subEntity, tableInfo.getKeyProperty(), masterKey);
//                    subUpdateEntity.addObject(subEntity);
//                }
//            } else {
//                Object subEntity = getConversionService().convert(values, subEntityRelation.getEntity());
//                if(!StringUtils.isEmpty(deleteFieldName)) {
//                    Object deleteValue = FieldUtils.getValueByName(values, deleteFieldName);
//                    if(Objects.nonNull(deleteValue)) {
//                        deleteSubEntity.addObject(subEntity);
//                        continue;
//                    }
//                }
//                Object masterKey = FieldUtils.getValueByName(entity, tableInfo.getKeyProperty());
//                FieldUtils.setFieldByName(subEntity, tableInfo.getKeyProperty(), masterKey);
//                subUpdateEntity.addObject(subEntity);
//            }
//            if(updateSubEntityStrategy == UpdateSubEntityStrategy.DELETE_BEFORE_INSERT) {
//                UpdateEntity deleteWrapperEntity = new UpdateEntity(subEntityRelation
//                        , physicDelete? UpdateCommand.PHYSIC_DELETE_WRAPPER : UpdateCommand.DELETE_WRAPPER
//                        , subService);
//                Object masterId = FieldUtils.getValueByName(entity, tableInfo.getKeyProperty());
//                QueryWrapper qw = new QueryWrapper();
//                qw.eq(tableInfo.getKeyColumn(), masterId);
//                deleteWrapperEntity.setWrapper(qw);
//                updateEntity.addSubUpdateEntity(deleteWrapperEntity);
//            } else {
//                if(deleteSubEntity.getObjList() != null && deleteSubEntity.getObjList().size() > 0) {
//                    updateEntity.addSubUpdateEntity(deleteSubEntity);
//                }
//            }
//            if(subUpdateEntity.getObjList() != null && subUpdateEntity.getObjList().size() > 0) {
//                updateEntity.addSubUpdateEntity(subUpdateEntity);
//            }
//        }
//
//        return updateEntityList;

        UpdateSubEntityStrategy updateSubEntityStrategy = ArrayParamUtils.parseArray(options, 0, UpdateSubEntityStrategy.DELETE_BEFORE_INSERT);
        boolean physicDelete = ArrayParamUtils.parseArray(options, 1, false);

        List<UpdateEntity> result = new ArrayList<>();
        result.add(super.createUpdateEntity(relation
                , object
                , updateSubEntityStrategy
                , physicDelete
                , false
                , false));
        return result;
    };
}
