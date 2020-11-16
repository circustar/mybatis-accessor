package com.circustar.mvcenhance.enhance.update;

import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.service.ISelectService;
import com.circustar.mvcenhance.enhance.utils.ArrayParamUtils;
import org.springframework.validation.BindingResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultDeleteEntityByIdProvider extends AutoDetectUpdateEntityProvider {
    @Override
    public String defineUpdateName() {
        return IUpdateEntityProvider.DELETE_BY_ID;
    }

    @Override
    public List<UpdateEntity> createUpdateEntities(EntityDtoServiceRelation relation, Object id, Object... options) throws Exception {
//        if(id == null) {
//            return null;
//        }
//        String[] subEntities = null;
//        boolean physicDelete = false;
//        if(options!= null && options.length > 0) {
//            if(options[0] != null) {
//                if(options[0].getClass().isArray()) {
//                    subEntities = (String[])options[0];
//                } else {
//                    subEntities = options[0].toString().replace(" ", "").split(",");
//                }
//            }
//            if(options.length > 1 && options != null) {
//                physicDelete = (boolean)options[1];
//            }
//        }
//
//        //TODO: Cache tableInfo
//        TableInfo tableInfo = TableInfoHelper.getTableInfo(relation.getEntity());
//        UpdateEntity updateEntity = new UpdateEntity(relation
//                , physicDelete ? UpdateCommand.PHYSIC_DELETE_ID: UpdateCommand.DELETE_ID
//                , applicationContext.getBean(relation.getService()));
//        updateEntity.setObjList(Collections.singletonList(id));
//
//        //TODO: Cache
//        DtoClassInfo dtoClassInfo = new DtoClassInfo(relation.getDto(), relation.getEntity());
//        if(subEntities == null) {
//            return Collections.singletonList(updateEntity);
//        }
//        // TODO: 解决嵌套
//        for(String subEntityName : subEntities) {
//            DtoField dtoField = dtoClassInfo.findDtoField(subEntityName);
//            if(dtoField == null) {
//                continue;
//            }
//            EntityDtoServiceRelation subEntityRelation = getRelationMap().getByDtoClass((Class)dtoField.getActualType());
//            if(subEntityRelation == null) {
//                continue;
//            }
//            UpdateEntity subUpdateEntity = new UpdateEntity(subEntityRelation
//                    , physicDelete ? UpdateCommand.PHYSIC_DELETE_WRAPPER: UpdateCommand.DELETE_WRAPPER
//                    , applicationContext.getBean(subEntityRelation.getService()));
//
//            QueryWrapper qw = new QueryWrapper();
//            qw.eq(tableInfo.getKeyColumn(), id);
//            subUpdateEntity.setWrapper(qw);
//            updateEntity.addSubUpdateEntity(subUpdateEntity);
//        }
//        return Collections.singletonList(updateEntity);
        List<String> defaultFields = relation.getDtoClassInfo().getFieldInfoList()
                .stream().map(x -> x.getFieldName()).collect(Collectors.toList());
        List<String> subEntities = ArrayParamUtils.parseArray(options, 0, defaultFields);

        boolean physicDelete = ArrayParamUtils.parseArray(options, 1, false);
        ISelectService selectService = applicationContext.getBean(ISelectService.class);
        Object object = selectService.getDtoById(relation, (Serializable) id
                , subEntities.toArray(new String[subEntities.size()]));
        List<UpdateEntity> result = new ArrayList<>();
        result.add(super.createUpdateEntity(relation, object
                , UpdateSubEntityStrategy.INSERT_OR_UPDATE
                , physicDelete, true, true));
        return result;
    }

    @Override
    public void validateAndSet(Object s, BindingResult bindingResult, Object... options){
    };
}
