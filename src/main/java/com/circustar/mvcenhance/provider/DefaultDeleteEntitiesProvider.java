package com.circustar.mvcenhance.provider;

import com.circustar.mvcenhance.field.DtoClassInfo;
import com.circustar.mvcenhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.field.DtoField;
import com.circustar.mvcenhance.utils.MvcEnhanceConstants;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.service.ISelectService;
import com.circustar.mvcenhance.provider.command.DeleteByIdBatchCommand;
import com.circustar.mvcenhance.utils.CommonCollectionUtils;
import com.circustar.mvcenhance.utils.FieldUtils;
import com.circustar.mvcenhance.utils.MapOptionUtils;
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
        List<UpdateEntity> result = Collections.emptyList();
        Collection values = CommonCollectionUtils.convertToCollection(ids);
        if(values.size() == 0) {return result;}

        String[] subEntities = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, new String[]{});
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
        boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, false);

        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDto());
        ISelectService selectService = applicationContext.getBean(ISelectService.class);

        String[] topEntities = this.getTopEntities(subEntities, ".");
        if(topEntities.length > 0) {
            for (Object id : values) {
                Object object = selectService.getDtoById(relation, (Serializable) id
                        , topEntities);
                for (String entityName : topEntities) {
                    DtoField dtoField = dtoClassInfo.getDtoField(entityName);
                    Object entity = FieldUtils.getValue(object, dtoField.getFieldTypeInfo().getField());
                    if (entity == null) {
                        continue;
                    }
                    String keyProperty = dtoField.getDtoClassInfo().getEntityClassInfo().getTableInfo().getKeyProperty();
                    List<Object> subIds = new ArrayList<>();
                    Collection entityList = CommonCollectionUtils.convertToCollection(entity);
                    if(entityList.size() == 0) {continue;}
                    for (Object obj : entityList) {
                        subIds.add(FieldUtils.getValueByName(obj, keyProperty));
                    }
                    Map newOptions = new HashMap(options);
                    newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
                    newOptions.put(MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, this.getSubEntities(subEntities
                            , entityName, "."));
                    result.addAll(this.createUpdateEntities(
                            dtoField.getEntityDtoServiceRelation()
                            , dtoClassInfoHelper
                            , subIds
                            , newOptions
                    ));
                }
            }
        }

        if(!updateChildrenOnly) {
            result.add(new UpdateEntity(applicationContext.getBean(relation.getService())
                    , DeleteByIdBatchCommand.getInstance()
                    , physicDelete
                    , dtoClassInfo.getEntityClassInfo()
                    , values
                    , true
                    , false));
        }
        return result;
    }

    @Override
    public void validateAndSet(Object s, BindingResult bindingResult, Map options){
    };
}
