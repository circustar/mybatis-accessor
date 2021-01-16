package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.MvcEnhanceConstants;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.service.ISelectService;
import com.circustar.mvcenhance.enhance.utils.MapOptionUtils;
import org.springframework.validation.BindingResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultDeleteEntityByIdProvider extends AutoDetectUpdateEntityProvider {
    @Override
    public String defineUpdateName() {
        return IUpdateEntityProvider.DELETE_BY_ID;
    }

    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation,
                                                   DtoClassInfoHelper dtoClassInfoHelper,
                                                   Object id, Map options) throws Exception {

        List<String> defaultFields = dtoClassInfoHelper.getDtoClassInfo(relation.getDto()).getSubDtoFieldList()
                .stream().map(x -> x.getFieldName()).collect(Collectors.toList());
        List<String> subEntities = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, defaultFields);
        Boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, true);
        ISelectService selectService = applicationContext.getBean(ISelectService.class);
        Object object = selectService.getDtoById(relation, (Serializable) id
                , subEntities.toArray(new String[subEntities.size()]));
        List<UpdateEntity> result = new ArrayList<>();
        result.add(super.createUpdateEntity(relation, dtoClassInfoHelper
                , object
                , UpdateSubEntityStrategy.INSERT_OR_UPDATE
                , physicDelete, true, true));
        return result;
    }

    @Override
    public void validateAndSet(Object s, BindingResult bindingResult, Map options){
    };
}
