package com.circustar.mybatisAccessor.provider;

import com.circustar.mybatisAccessor.classInfo.DtoClassInfo;
import com.circustar.mybatisAccessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatisAccessor.classInfo.DtoField;
import com.circustar.mybatisAccessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatisAccessor.utils.MvcEnhanceConstants;
import com.circustar.mybatisAccessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatisAccessor.provider.command.InsertCommand;
import com.circustar.mybatisAccessor.utils.CollectionUtils;
import com.circustar.mybatisAccessor.utils.FieldUtils;
import com.circustar.mybatisAccessor.utils.MapOptionUtils;

import java.util.*;

public class DefaultInsertEntityProvider extends AbstractUpdateEntityProvider {
    private static DefaultInsertEntityProvider instance = new DefaultInsertEntityProvider();
    public static DefaultInsertEntityProvider getInstance() {
        return instance;
    }

    @Override
    public List<DefaultEntityCollectionUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, Map options) throws Exception {
        List<DefaultEntityCollectionUpdateProcessor> result = new ArrayList<>();
        Collection values = CollectionUtils.convertToCollection(dto);
        if(values.size() == 0) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());
        boolean insertAllEntities = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_INSERT_ALL_CHILDREN, false);
        String[] children;
        if(insertAllEntities) {
            children = CollectionUtils.convertStreamToStringArray(dtoClassInfo.getSubDtoFieldList().stream().map(x -> x.getField().getName()));
        } else {
            children = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, new String[]{});
        }
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);

        String[] topEntities = this.getTopEntities(children, ".");
        boolean hasChildren = false;

        List<Object> updateTargetList = new ArrayList<>();
        for(Object value : values) {
            if(dtoClassInfo.getVersionField() != null) {
                FieldUtils.setField(value
                        , dtoClassInfo.getVersionField().getEntityFieldInfo().getField()
                        , dtoClassInfo.getVersionDefaultValue());
            }
            Object updateTarget = dtoClassInfoHelper.convertToEntity(value);
            updateTargetList.add(updateTarget);
            DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdater = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , InsertCommand.getInstance()
                    , null
                    , dtoClassInfo.getEntityClassInfo()
                    , Collections.singleton(updateTarget)
                    , false
                    , updateChildrenOnly);
            for(String entityName : topEntities) {
                DtoField dtoField = dtoClassInfo.getDtoField(entityName);
                Object subValue = FieldUtils.getValue(value, dtoField.getEntityFieldInfo().getField());
                Collection childList = CollectionUtils.convertToCollection(subValue);
                if(childList.size() == 0) {continue;}
                hasChildren = true;
                Map newOptions = new HashMap(options);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, this.getChildren(children
                        , entityName, "."));
                defaultEntityCollectionUpdater.addSubUpdateEntities(this.createUpdateEntities(
                        dtoField.getEntityDtoServiceRelation()
                        , dtoClassInfoHelper, childList, newOptions));
            }
            result.add(defaultEntityCollectionUpdater);
        }

        if(!hasChildren) {
            if(updateChildrenOnly) {
                return Collections.emptyList();
            } else {
                DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdater = new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                        , InsertCommand.getInstance()
                        , null
                        , dtoClassInfo.getEntityClassInfo()
                        , updateTargetList
                        , false
                        , false);
                return Collections.singletonList(defaultEntityCollectionUpdater);
            }
        }

        return result;
    };

}
