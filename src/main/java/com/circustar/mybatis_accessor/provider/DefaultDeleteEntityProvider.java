package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.provider.command.DeleteByIdBatchCommand;
import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.common_utils.collection.MapOptionUtils;

import java.io.Serializable;
import java.util.*;

public class DefaultDeleteEntityProvider extends AbstractUpdateEntityProvider {
    private static DefaultDeleteEntityProvider instance = new DefaultDeleteEntityProvider();
    public static DefaultDeleteEntityProvider getInstance() {
        return instance;
    }
    @Override
    public List<IEntityUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object ids, Map options)
    {
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        Collection values = CollectionUtils.convertToCollection(ids);;
        if(values.size() == 0) {return result;}

        String[] children = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_LIST, new String[]{});
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
        boolean physicDelete = getPhysicDelete(dtoClassInfoHelper, relation);

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());
        ISelectService selectService = this.getSelectService();

        String[] topEntities = this.getTopEntities(children, ".");
        if(topEntities.length > 0) {
            for (Object id : values) {
                if(id == null) {
                    continue;
                }
                Object object = selectService.getDtoById(relation, (Serializable) id
                        , topEntities);
                for (String entityName : topEntities) {
                    DtoField dtoField = dtoClassInfo.getDtoField(entityName);
                    Object entity = FieldUtils.getFieldValue(object, dtoField.getEntityFieldInfo().getReadMethod());
                    if (entity == null) {
                        continue;
                    }
                    DtoField dtoKeyField = dtoClassInfo.getKeyField();
                    List<Object> subIds = new ArrayList<>();
                    Collection entityList = CollectionUtils.convertToCollection(entity);
                    if(entityList.size() == 0) {continue;}
                    for (Object obj : entityList) {
                        if(dtoKeyField != null) {
                            Object subId = FieldUtils.getFieldValue(obj, dtoKeyField.getReadMethod());
                            if(subId != null) {
                                subIds.add(subId);
                            }
                        }
                    }
                    Map newOptions = new HashMap(options);
                    newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
                    newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_LIST, this.getChildren(children
                            , entityName, "."));
                    result.addAll(this.createUpdateEntities(
                            dtoField.getEntityDtoServiceRelation()
                            , dtoClassInfoHelper, subIds, newOptions
                    ));
                }
            }
        }

        if(!updateChildrenOnly) {
            result.add(new DefaultEntityCollectionUpdateProcessor(relation.getServiceBean(applicationContext)
                    , DeleteByIdBatchCommand.getInstance()
                    , physicDelete
                    , null //dtoClassInfo.getEntityClassInfo()
                    , Arrays.asList(values.toArray())
                    , true
                    , false));
        }
        return result;
    }
}
