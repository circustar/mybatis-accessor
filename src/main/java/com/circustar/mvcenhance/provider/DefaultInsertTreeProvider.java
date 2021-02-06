package com.circustar.mvcenhance.provider;

import com.circustar.mvcenhance.classInfo.DtoClassInfo;
import com.circustar.mvcenhance.classInfo.DtoClassInfoHelper;
import com.circustar.mvcenhance.classInfo.DtoField;
import com.circustar.mvcenhance.utils.MvcEnhanceConstants;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.provider.command.InsertCommand;
import com.circustar.mvcenhance.utils.CollectionUtils;
import com.circustar.mvcenhance.utils.FieldUtils;
import com.circustar.mvcenhance.utils.MapOptionUtils;

import java.util.*;

public class DefaultInsertTreeProvider extends AbstractUpdateTreeProvider {
    private static DefaultInsertTreeProvider instance = new DefaultInsertTreeProvider();
    public static DefaultInsertTreeProvider getInstance() {
        return instance;
    }

    @Override
    public Collection<UpdateTree> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object dto, Map options) throws Exception {
        List<UpdateTree> result = new ArrayList<>();
        Collection values = CollectionUtils.convertToCollection(dto);
        if(values.size() == 0) {return result;}

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());
        boolean insertAllEntities = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_INSERT_ALL_CHILDREN, false);
        String[] children;
        if(insertAllEntities) {
            children = CollectionUtils.convertStreamToStringArray(dtoClassInfo.getSubDtoFieldList().stream().map(x -> x.getFieldName()));
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
                        , dtoClassInfo.getVersionField().getFieldTypeInfo().getField()
                        , dtoClassInfo.getVersionDefaultValue());
            }
            Object updateTarget = dtoClassInfoHelper.convertToEntity(value);
            updateTargetList.add(updateTarget);
            UpdateTree updateTree = new UpdateTree(relation.getServiceBean(applicationContext)
                    , InsertCommand.getInstance()
                    , null
                    , dtoClassInfo.getEntityClassInfo()
                    , Collections.singleton(updateTarget)
                    , false
                    , updateChildrenOnly);
            for(String entityName : topEntities) {
                DtoField dtoField = dtoClassInfo.getDtoField(entityName);
                Object subValue = FieldUtils.getValue(value, dtoField.getFieldTypeInfo().getField());
                Collection childList = CollectionUtils.convertToCollection(subValue);
                if(childList.size() == 0) {continue;}
                hasChildren = true;
                Map newOptions = new HashMap(options);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, this.getChildren(children
                        , entityName, "."));
                updateTree.addSubUpdateEntities(this.createUpdateEntities(
                        dtoField.getEntityDtoServiceRelation()
                        , dtoClassInfoHelper, childList, newOptions));
            }
            result.add(updateTree);
        }

        if(!hasChildren) {
            if(updateChildrenOnly) {
                return Collections.emptyList();
            } else {
                UpdateTree updateTree = new UpdateTree(relation.getServiceBean(applicationContext)
                        , InsertCommand.getInstance()
                        , null
                        , dtoClassInfo.getEntityClassInfo()
                        , updateTargetList
                        , false
                        , false);
                return Collections.singletonList(updateTree);
            }
        }

        return result;
    };

}
