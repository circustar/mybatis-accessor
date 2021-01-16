package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.MvcEnhanceConstants;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.utils.MapOptionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultSaveUpdateDeleteEntitiesProvider extends AutoDetectUpdateEntityProvider {
    @Override
    public String defineUpdateName() {
        return IUpdateEntityProvider.SAVE_UPDATE_DELETE_LIST;
    }

    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation
            , DtoClassInfoHelper dtoClassInfoHelper, Object object, Map options) throws Exception {

        UpdateSubEntityStrategy updateSubEntityStrategy = MapOptionUtils.getValue(options, UpdateSubEntityStrategy.class, UpdateSubEntityStrategy.DELETE_BEFORE_INSERT);
        Boolean physicDelete = MapOptionUtils.getValue(options, MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, true);

        List<UpdateEntity> result = new ArrayList<>();
        Collection c = (Collection) object;
        for(Object o : c) {
            result.add(super.createUpdateEntity(relation, dtoClassInfoHelper, o
                    , updateSubEntityStrategy
                    , physicDelete, true, false));
        }
        // TODO: Batch Save Or Update
//        Map<Boolean, List<UpdateEntity>> collect = result.stream().collect(Collectors.partitioningBy(x -> x.getWrapper() == null
//                && (Objects.isNull(x.getObjList()) || x.getObjList().size() == 0)));
//        Map<UpdateCommand, List<UpdateEntity>> collect1 = collect.get(true).stream().collect(Collectors.groupingBy(x -> x.getUpdateCommand()));

        return result;
    }

}
