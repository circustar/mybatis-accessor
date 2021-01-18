package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.field.DtoClassInfo;
import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.field.DtoField;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.MvcEnhanceConstants;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.update.command.InsertCommand;
import com.circustar.mvcenhance.enhance.utils.FieldUtils;
import com.circustar.mvcenhance.enhance.utils.MapOptionUtils;

import java.util.*;

public class DefaultInsertEntitiesEntityProvider extends AbstractUpdateEntityProvider {
    private static DefaultInsertEntitiesEntityProvider instance = new DefaultInsertEntitiesEntityProvider();
    public static DefaultInsertEntitiesEntityProvider getInstance() {
        return instance;
    }

    @Override
    public Collection<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object object, Map options) throws Exception {
        boolean updateChildrenOnly = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, false);
        Collection values;
        if(Collection.class.isAssignableFrom(object.getClass())) {
            values = (Collection) object;
        } else {
            values = Collections.singleton(object);
        }
        List<UpdateEntity> result = new ArrayList<>();
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relation.getDto());
        for(Object value : values) {
            UpdateEntity updateEntity = new UpdateEntity(applicationContext.getBean(relation.getService())
                    , InsertCommand.getInstance()
                    , null
                    , dtoClassInfo.getEntityClassInfo()
                    , updateChildrenOnly?Collections.singleton(value):null);

            List<DtoField> subDtoFieldList = dtoClassInfo.getSubDtoFieldList();
            for(DtoField dtoField : subDtoFieldList) {
                Object subValue = FieldUtils.getValue(value, dtoField.getFieldTypeInfo().getField());
                if(subValue == null) continue;
                updateEntity.addSubUpdateEntities(this.createUpdateEntities(dtoField.getDtoClassInfo().getEntityDtoServiceRelation()
                        , dtoClassInfoHelper, subValue, options));
            }
            result.add(updateEntity);
        }

        return result;
    };

}
