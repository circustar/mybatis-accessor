package com.circustar.mybatis_accessor.provider;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import com.circustar.mybatis_accessor.utils.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.provider.command.DeleteByIdBatchCommand;
import com.circustar.mybatis_accessor.utils.CollectionUtils;
import com.circustar.mybatis_accessor.utils.FieldUtils;
import com.circustar.mybatis_accessor.utils.MapOptionUtils;

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
            throws Exception {
        List<IEntityUpdateProcessor> result = new ArrayList<>();
        List values = Arrays.asList(ids);
        if(values.size() == 0) {return result;}

        String[] children = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, new String[]{});
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
        boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, false);

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());
        ISelectService selectService = this.getSelectService();

        String[] topEntities = this.getTopEntities(children, ".");
        if(topEntities.length > 0) {
            for (Object id : values) {
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
                    newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, this.getChildren(children
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
                    , values
                    , true
                    , false));
        }
        return result;
    }
}
