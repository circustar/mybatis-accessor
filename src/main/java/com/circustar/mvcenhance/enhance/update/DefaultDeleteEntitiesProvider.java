package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.field.DtoClassInfo;
import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.field.DtoField;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.MvcEnhanceConstants;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.service.ISelectService;
import com.circustar.mvcenhance.enhance.update.command.DeleteByIdBatchCommand;
import com.circustar.mvcenhance.enhance.update.command.DeleteByIdCommand;
import com.circustar.mvcenhance.enhance.utils.FieldUtils;
import com.circustar.mvcenhance.enhance.utils.MapOptionUtils;
import org.springframework.validation.BindingResult;

import java.io.Serializable;
import java.util.*;

public class DefaultDeleteEntitiesProvider extends AbstractUpdateEntityProvider {
    private static DefaultDeleteEntitiesProvider instance = new DefaultDeleteEntitiesProvider();
    public static DefaultDeleteEntitiesProvider getInstance() {
        return instance;
    }
    @Override
    public Collection<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation,
                                                   DtoClassInfoHelper dtoClassInfoHelper,
                                                   Object ids, Map options) throws Exception {
        String[] subEntities = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, new String[]{});
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
        boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, false);

        boolean batchUpdate = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_BATCH_FLAG, false);

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDto());
        Collection values;
        if(Collection.class.isAssignableFrom(ids.getClass())) {
            values = (Collection) ids;
        } else {
            values = Collections.singleton(ids);
        }

        List<UpdateEntity> updateEntityCollection = new ArrayList<>();
        ISelectService selectService = applicationContext.getBean(ISelectService.class);

        for(Object id : values) {
            UpdateEntity updateEntity = new UpdateEntity(applicationContext.getBean(relation.getService())
                    , batchUpdate? DeleteByIdBatchCommand.getInstance() :DeleteByIdCommand.getInstance()
                    , physicDelete
                    , dtoClassInfo.getEntityClassInfo()
                    , Collections.singleton(id)
                    , true
                    , updateChildrenOnly);

            Object object = selectService.getDtoById(relation, (Serializable) id
                    , subEntities);
            for (DtoField dtoField : dtoClassInfo.getSubDtoFieldList()) {
                Object subEntity = FieldUtils.getValue(object, dtoField.getFieldTypeInfo().getField());
                if (subEntity == null) {
                    continue;
                }
                String keyProperty = dtoField.getDtoClassInfo().getEntityClassInfo().getTableInfo().getKeyProperty();
                List<Object> subIds = new ArrayList<>();
                if (Collection.class.isAssignableFrom(subEntity.getClass())) {
                    for (Object obj : (Collection) subEntity) {
                        subIds.add(FieldUtils.getValueByName(obj, keyProperty));
                    }
                } else {
                    subIds.add(FieldUtils.getValueByName(subEntity, keyProperty));
                }
                Map newOptions = new HashMap(options);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, true);
                newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, this.getSubEntities(subEntities
                        , dtoField.getFieldName(), "."));
                updateEntityCollection.addAll(this.createUpdateEntities(
                        dtoField.getEntityDtoServiceRelation()
                        , dtoClassInfoHelper
                        , subIds
                        , newOptions
                ));
            }
            updateEntityCollection.add(updateEntity);
        }
        return updateEntityCollection;
    }

    @Override
    public void validateAndSet(Object s, BindingResult bindingResult, Map options){
    };
}
