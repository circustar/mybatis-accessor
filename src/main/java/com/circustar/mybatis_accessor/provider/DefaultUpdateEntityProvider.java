package com.circustar.mybatis_accessor.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.parameter.DefaultAbstractUpdateProviderParam;
import com.circustar.mybatis_accessor.provider.parameter.DefaultDeleteProviderParam;
import com.circustar.mybatis_accessor.provider.parameter.DefaultInsertProviderParam;
import com.circustar.mybatis_accessor.provider.parameter.DefaultUpdateProviderParam;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.provider.command.*;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.common_utils.collection.MapOptionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

public class DefaultUpdateEntityProvider extends AbstractUpdateEntityProvider<DefaultUpdateProviderParam> {
    private static DefaultUpdateEntityProvider instance = new DefaultUpdateEntityProvider();
    public static DefaultUpdateEntityProvider getInstance() {
        return instance;
    }

    @Override
    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, DefaultUpdateProviderParam options) {
        return this.createUpdateEntities(relation, dtoClassInfoHelper, dto, options, new HashSet());
    }

    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, DefaultUpdateProviderParam options, Set updateTargetSet) {
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        Collection values = CollectionUtils.convertToCollection(dto);
        values.removeAll(updateTargetSet);
        updateTargetSet.addAll(values);
        if(values.isEmpty()) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());

        boolean updateChildrenOnly = options.isUpdateChildrenOnly();
        boolean deleteBeforeUpdate = options.isDeleteBeforeUpdate();
        boolean includeAllChildren = options.isIncludeAllChildren();
        String[] children;
        if(includeAllChildren) {
            children = CollectionUtils.convertStreamToStringArray(dtoClassInfo.getSubDtoFieldList().stream().map(x -> x.getField().getName()));
        } else {
            children = options.getUpdateChildrenNames();
        }
        boolean physicDelete = dtoClassInfo.isPhysicDelete();

        DefaultDeleteEntityProvider defaultDeleteTreeProvider =  DefaultDeleteEntityProvider.getInstance();
        DefaultInsertEntityProvider insertEntitiesEntityProvider = DefaultInsertEntityProvider.getInstance();
        List<IEntityUpdateProcessor> defaultEntityCollectionUpdaterCollection = new ArrayList<>();
        String keyColumn = dtoClassInfo.getEntityClassInfo().getTableInfo().getKeyColumn();

        String[] topEntities = this.getTopEntities(dtoClassInfo, children);

        for(Object value : values) {
            if(value == null) {
                continue;
            }
            Object updateTarget = dtoClassInfoHelper.convertToEntity(value);

            DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdater = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , UpdateByIdCommand.getInstance()
                    , null
                    , dtoClassInfo.getEntityClassInfo()
                    , Collections.singletonList(updateTarget)
                    , false
                    , updateChildrenOnly);
            Object keyValue = FieldUtils.getFieldValue(value, dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod());

            for(String entityName : topEntities) {
                DtoField subDtoField = dtoClassInfo.getDtoField(entityName);
                Object topChild = FieldUtils.getFieldValue(value, subDtoField.getPropertyDescriptor().getReadMethod());
                if(deleteBeforeUpdate) {
                    QueryWrapper qw = new QueryWrapper();
                    qw.eq(keyColumn, keyValue);
                    defaultEntityCollectionUpdater.addSubUpdateEntity(new DefaultEntityCollectionUpdateProcessor(subDtoField.getEntityDtoServiceRelation().getServiceBean(applicationContext)
                            , DeleteWrapperCommand.getInstance()
                            , physicDelete
                            , null
                            , Collections.singletonList(qw)
                            , true
                            , false));
                }
                Collection childList = CollectionUtils.convertToCollection(topChild);
                if(childList.isEmpty()) {continue;}
                String[] subChildren = this.getChildren(children, entityName, ".");
                DtoClassInfo subDtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(subDtoField.getEntityDtoServiceRelation().getDtoClass());
                for(Object child : childList) {
                    Object subEntityKeyValue = FieldUtils.getFieldValue(child, subDtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod());
                    if(subEntityKeyValue != null) {
                        Object deleteFlagValue = null;
                        if(subDtoClassInfo.getDeleteFlagField() != null) {
                            deleteFlagValue = FieldUtils.getFieldValue(child, subDtoClassInfo.getDeleteFlagField().getPropertyDescriptor().getReadMethod());
                        }
                        if(!StringUtils.isEmpty(deleteFlagValue) && !"0".equals(deleteFlagValue.toString())) {
                            DefaultDeleteProviderParam newParam = new DefaultDeleteProviderParam(false, subChildren);
                            defaultEntityCollectionUpdater.addSubUpdateEntities(defaultDeleteTreeProvider.createUpdateEntities(
                                    subDtoField.getEntityDtoServiceRelation(), dtoClassInfoHelper
                                    , subEntityKeyValue, newParam
                            ));
                        } else {
                            DefaultUpdateProviderParam newParam = new DefaultUpdateProviderParam(false, includeAllChildren, subChildren, deleteBeforeUpdate);
                            defaultEntityCollectionUpdater.addSubUpdateEntities(this.createUpdateEntities(
                                    subDtoField.getEntityDtoServiceRelation(), dtoClassInfoHelper
                                    , child, newParam, updateTargetSet
                            ));
                        }
                    } else {
                        DefaultInsertProviderParam newParam = new DefaultInsertProviderParam(false, includeAllChildren, subChildren);
                        defaultEntityCollectionUpdater.addSubUpdateEntities(
                                insertEntitiesEntityProvider.createUpdateEntities(subDtoField.getEntityDtoServiceRelation()
                                , dtoClassInfoHelper, child, newParam, updateTargetSet));
                    }
                }
            }
            defaultEntityCollectionUpdaterCollection.add(defaultEntityCollectionUpdater);
        }
        return defaultEntityCollectionUpdaterCollection;
    }
}
