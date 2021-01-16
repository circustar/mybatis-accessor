package com.circustar.mvcenhance.enhance.update;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.field.DtoField;
import com.circustar.mvcenhance.enhance.field.EntityClassInfo;
import com.circustar.mvcenhance.enhance.field.FieldTypeInfo;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.MvcEnhanceConstants;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.utils.*;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class AutoDetectUpdateEntityProvider extends AbstractUpdateEntityProvider {
    @Override
    public String defineUpdateName() {
        return IUpdateEntityProvider.AUTO_DETECT_UPDATE_ENTITY;
    }

    private Map newOptions = null;

    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper
            , Object object, Map options) throws Exception {
        if(object == null ) {
            return null;
        }

        UpdateSubEntityStrategy updateSubEntityStrategy = MapOptionUtils.getValue(options, UpdateSubEntityStrategy.class, UpdateSubEntityStrategy.DELETE_BEFORE_INSERT);
        Boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, true);
        Boolean updateTarget = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_TARGET, true);
        Boolean parentDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PARENT_DELETE, false);

        this.newOptions = MapOptionUtils.copy(options);
        this.newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_TARGET, true);

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

        FieldTypeInfo deleteFieldTypeInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDto()).getDeleteFieldTypeInfo();
        EntityClassInfo entityClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDto()).getEntityClassInfo();
        TableInfo tableInfo = entityClassInfo.getTableInfo();
        UpdateEntity updateEntity = new UpdateEntity(relation, UpdateCommand.SAVE_OR_UPDATE
                , applicationContext.getBean(relation.getService()));
        Object entity = dtoClassInfoHelper.convertToEntity(object);
        this.setDefaultVersion(tableInfo, entity);
        this.setDefaultLogicDelete(tableInfo, entity);
        //Object entity = getConversionService().convert(object, relation.getEntity());
        Boolean isDeleteObject = false;
        if(updateTarget) {
            if(parentDelete) {
                isDeleteObject = true;
            } else if(deleteFieldTypeInfo != null) {
                Object deleteValue = FieldUtils.getValue(object, deleteFieldTypeInfo.getField());
                isDeleteObject = !StringUtils.isEmpty(deleteValue);
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

    protected void setDefaultVersion(TableInfo tableInfo, Object entity) throws NoSuchFieldException, IllegalAccessException {
        if(tableInfo.isWithVersion() && FieldUtils.getValue(entity, tableInfo.getVersionFieldInfo().getField()) == null) {
            Class<?> propertyType = tableInfo.getVersionFieldInfo().getPropertyType();
            if(Date.class == propertyType) {
                FieldUtils.setField(entity, tableInfo.getVersionFieldInfo().getField(), new Date());
            } else if(Timestamp.class == propertyType) {
                FieldUtils.setField(entity, tableInfo.getVersionFieldInfo().getField(), Timestamp.from(new Date().toInstant()));
            } else if(LocalDateTime.class == propertyType) {
                FieldUtils.setField(entity, tableInfo.getVersionFieldInfo().getField(), LocalDateTime.now());
            } else {
                FieldUtils.setField(entity, tableInfo.getVersionFieldInfo().getField(), 1);
            }
        }
    }

    protected void setDefaultLogicDelete(TableInfo tableInfo, Object entity) throws NoSuchFieldException, IllegalAccessException {
        if(tableInfo.isWithLogicDelete() && FieldUtils.getValue(entity, tableInfo.getLogicDeleteFieldInfo().getField()) == null) {
            Class<?> propertyType = tableInfo.getLogicDeleteFieldInfo().getPropertyType();
            Object logicNotDeleteValue = null;
            if(String.class == propertyType) {
                logicNotDeleteValue = tableInfo.getLogicDeleteFieldInfo().getLogicNotDeleteValue();
            } else {
                logicNotDeleteValue = Integer.valueOf(tableInfo.getLogicDeleteFieldInfo().getLogicNotDeleteValue());
            }
            FieldUtils.setField(entity, tableInfo.getLogicDeleteFieldInfo().getField(), logicNotDeleteValue);
        }
    }

    protected List<UpdateEntity> createSubUpdateEntity(EntityDtoServiceRelation parentRelation
            , DtoClassInfoHelper dtoClassInfoHelper
            , Object object, Object parentKeyValue
            , UpdateSubEntityStrategy updateSubEntityStrategy
            , boolean isParentDeleted, boolean physicDelete) throws Exception {
        List<UpdateEntity> updateEntities= new ArrayList<>();

        List<DtoField> dtoFields = dtoClassInfoHelper.getDtoClassInfo(parentRelation.getDto()).getSubDtoFieldList();
        for(DtoField dtoField : dtoFields) {
            Object subObject = FieldUtils.getValue(object, dtoField.getFieldTypeInfo().getField());
            if(subObject == null) {continue;}
            FieldUtils.setFieldByNameWithCollection(subObject, parentRelation.getTableInfo().getKeyProperty(), parentKeyValue);
            EntityDtoServiceRelation subRelation = getRelationMap().getByDtoClass((Class)dtoField.getFieldTypeInfo().getActualType());
            IService subService = applicationContext.getBean(subRelation.getService());
            if(updateSubEntityStrategy == UpdateSubEntityStrategy.DELETE_BEFORE_INSERT) {
                UpdateEntity deleteWrapperEntity = new UpdateEntity(subRelation
                        , physicDelete? UpdateCommand.PHYSIC_DELETE_WRAPPER : UpdateCommand.DELETE_WRAPPER
                        , subService);
                QueryWrapper qw = new QueryWrapper();
                qw.eq(parentRelation.getTableInfo().getKeyColumn(), parentKeyValue);
                deleteWrapperEntity.setWrapper(qw);
                updateEntities.add(deleteWrapperEntity);
            }
            updateEntities.addAll(this.createUpdateEntities(subRelation, dtoClassInfoHelper, subObject, this.newOptions));
        }
        return updateEntities;
    }
}
