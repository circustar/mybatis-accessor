package com.circustar.mvcenhance.enhance.update;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.field.DtoField;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.utils.*;
import org.springframework.util.StringUtils;

import java.util.*;

public class AutoDetectUpdateEntityProvider extends AbstractUpdateEntityProvider {
    @Override
    public String defineUpdateName() {
        return IUpdateEntityProvider.AUTO_DETECT_UPDATE_ENTITY;
    }

    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper
            , Object object, Object... options) throws Exception {
        if(object == null ) {
            return null;
        }

        UpdateSubEntityStrategy updateSubEntityStrategy = ArrayParamUtils.parseArray(options, 0, UpdateSubEntityStrategy.DELETE_BEFORE_INSERT);
        Boolean physicDelete = ArrayParamUtils.parseArray(options, 1, true);
        Boolean updateTarget = ArrayParamUtils.parseArray(options, 2, true);
        boolean parentDelete = ArrayParamUtils.parseArray(options, 3, false);

        List<UpdateEntity> result = new ArrayList<>();
        if(Collection.class.isAssignableFrom(object.getClass())) {
            Collection c = (Collection) object;
            for(Object o : c) {
                result.add(createUpdateEntity(relation, dtoClassInfoHelper, o, updateSubEntityStrategy, physicDelete, updateTarget, parentDelete));
            }
        } else {
            result.add(createUpdateEntity(relation, dtoClassInfoHelper, object, updateSubEntityStrategy, physicDelete, updateTarget, parentDelete));
        }
        return result;
    }

    protected UpdateEntity createUpdateEntity(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper
            , Object object, UpdateSubEntityStrategy updateSubEntityStrategy
            , Boolean physicDelete, Boolean updateTarget, boolean parentDelete) throws Exception {

        String deleteFieldName = AnnotationUtils.getDeleteFieldAnnotationValue(relation.getDto());
        UpdateEntity updateEntity = new UpdateEntity(relation, UpdateCommand.SAVE_OR_UPDATE
                , applicationContext.getBean(relation.getService()));
        Object entity = getConversionService().convert(object, relation.getEntity());
        Boolean isDeleteObject = false;
        if(updateTarget) {
            if(parentDelete) {
                isDeleteObject = true;
            } else if(!StringUtils.isEmpty(deleteFieldName)) {
                Object deleteValue = FieldUtils.getValueByName(object, deleteFieldName);
                isDeleteObject = Objects.nonNull(deleteValue);
            }
            if(isDeleteObject) {
                updateEntity.setUpdateCommand(physicDelete?UpdateCommand.PHYSIC_DELETE_ID : UpdateCommand.DELETE_ID);
            }
            updateEntity.addObject(entity);
        }
        Object parentKeyValue = FieldUtils.getValueByName(entity, relation.getTableInfo().getKeyProperty());
        updateEntity.addSubUpdateEntities(this.createSubUpdateEntity(relation,
                dtoClassInfoHelper, object
                , parentKeyValue, updateSubEntityStrategy
                , isDeleteObject, physicDelete));
        return updateEntity;
    }

    protected List<UpdateEntity> createSubUpdateEntity(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper
            , Object object, Object parentKeyValue
            , UpdateSubEntityStrategy updateSubEntityStrategy
            , boolean isParentDeleted, boolean physicDelete) throws Exception {
        List<UpdateEntity> updateEntities= new ArrayList<>();

        List<DtoField> dtoFields = dtoClassInfoHelper.getDtoClassInfo(relation.getDto()).getSubDtoFieldList();
        for(DtoField dtoField : dtoFields) {
            Object subObject = FieldUtils.getValue(object, dtoField.getFieldTypeInfo().getField());
            if(subObject == null) {continue;}
            FieldUtils.setFieldByNameWithCollection(subObject, relation.getTableInfo().getKeyProperty(), parentKeyValue);
            EntityDtoServiceRelation subRelation = getRelationMap().getByDtoClass((Class)dtoField.getFieldTypeInfo().getActualType());
            IService subService = applicationContext.getBean(subRelation.getService());
            if(updateSubEntityStrategy == UpdateSubEntityStrategy.DELETE_BEFORE_INSERT) {
                UpdateEntity deleteWrapperEntity = new UpdateEntity(subRelation
                        , physicDelete? UpdateCommand.PHYSIC_DELETE_WRAPPER : UpdateCommand.DELETE_WRAPPER
                        , subService);
                QueryWrapper qw = new QueryWrapper();
                qw.eq(relation.getTableInfo().getKeyColumn(), parentKeyValue);
                deleteWrapperEntity.setWrapper(qw);
                updateEntities.add(deleteWrapperEntity);
            }

            Object[] options = new Object[] {
                    updateSubEntityStrategy, physicDelete, true, isParentDeleted
            };
            updateEntities.addAll(this.createUpdateEntities(subRelation, dtoClassInfoHelper, subObject, options));
        }
        return updateEntities;
    }
}
