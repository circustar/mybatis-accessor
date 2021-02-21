package com.circustar.mvcenhance.provider;

import com.circustar.mvcenhance.classInfo.DtoClassInfo;
import com.circustar.mvcenhance.classInfo.DtoClassInfoHelper;
import com.circustar.mvcenhance.classInfo.DtoField;
import com.circustar.mvcenhance.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mvcenhance.utils.MvcEnhanceConstants;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.service.ISelectService;
import com.circustar.mvcenhance.provider.command.DeleteByIdBatchCommand;
import com.circustar.mvcenhance.utils.CollectionUtils;
import com.circustar.mvcenhance.utils.FieldUtils;
import com.circustar.mvcenhance.utils.MapOptionUtils;

import java.io.Serializable;
import java.util.*;

public class DefaultDeleteEntityProvider extends AbstractUpdateEntityProvider {
    private static DefaultDeleteEntityProvider instance = new DefaultDeleteEntityProvider();
    public static DefaultDeleteEntityProvider getInstance() {
        return instance;
    }
    @Override
    public List<DefaultEntityCollectionUpdateProcessor> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object ids, Map options)
            throws Exception {
        List<DefaultEntityCollectionUpdateProcessor> result = new ArrayList<>();
        Collection values = CollectionUtils.convertToCollection(ids);
        if(values.size() == 0) {return result;}

        String[] children = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, new String[]{});
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
        boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, false);

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDtoClass());
        ISelectService selectService = this.getSelectService();

        String[] topEntities = this.getTopEntities(children, ".");
        if(topEntities.length > 0) {
            for (Object id : values) {
                Object object = selectService.getById(relation, (Serializable) id
                        , topEntities);
                for (String entityName : topEntities) {
                    DtoField dtoField = dtoClassInfo.getDtoField(entityName);
                    Object entity = FieldUtils.getValue(object, dtoField.getFieldTypeInfo().getField());
                    if (entity == null) {
                        continue;
                    }
                    String keyProperty = dtoField.getDtoClassInfo().getEntityClassInfo().getTableInfo().getKeyProperty();
                    List<Object> subIds = new ArrayList<>();
                    Collection entityList = CollectionUtils.convertToCollection(entity);
                    if(entityList.size() == 0) {continue;}
                    for (Object obj : entityList) {
                        subIds.add(FieldUtils.getValueByName(obj, keyProperty));
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
