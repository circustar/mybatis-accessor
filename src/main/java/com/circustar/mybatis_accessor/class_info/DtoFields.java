package com.circustar.mybatis_accessor.class_info;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.reflection.ClassUtils;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.common_utils.parser.SPELParser;

import java.io.Serializable;
import java.util.*;

public abstract class DtoFields {
    public static void assignDtoField(DtoClassInfoHelper dtoClassInfoHelper, Object obj, DtoField dtoField, List<Object> values) {
        if(!dtoField.getCollection()) {
            FieldUtils.setFieldValue(obj, dtoField.getPropertyDescriptor().getWriteMethod(), (values == null || values.isEmpty())?
                    null : dtoClassInfoHelper.convertFromEntity(values.get(0), dtoField.getFieldDtoClassInfo()));
            return;
        }
        DtoField.SupportGenericType supportGenericType = DtoField.SupportGenericType.getSupportGenericType((Class) dtoField.getOwnClass());
        if(supportGenericType == null) {
            return;
        }
        Collection collection = ClassUtils.createInstance(supportGenericType.getTargetClass());
        for (Object var0 : values) {
            collection.add(dtoClassInfoHelper.convertFromEntity(var0, dtoField.getFieldDtoClassInfo()));
        }
        FieldUtils.setFieldValue(obj, dtoField.getPropertyDescriptor().getWriteMethod(), collection);
    }

    public static void queryAndAssignDtoFieldById(DtoClassInfo dtoClassInfo
            , EntityDtoServiceRelation relationInfo
            , List<DtoField> dtoFields
            , Object dto
            , Serializable dtoId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        for(DtoField dtoField : dtoFields) {
            EntityDtoServiceRelation childInfo = dtoField.getEntityDtoServiceRelation();
            if(childInfo == null) {
                continue;
            }
            DtoClassInfo subDtoClassInfo = dtoClassInfo.getDtoClassInfoHelper().getDtoClassInfo(childInfo);
            DtoField subDtoClassInfoDtoField = null;
            DtoField masterDtoClassInfoDtoField = null;
            if(subDtoClassInfo.equals(dtoClassInfo) && dtoClassInfo.getIdReferenceField() != null) {
                if(Collection.class.isAssignableFrom(dtoField.getOwnClass())) {
                    subDtoClassInfoDtoField =  dtoClassInfo.getIdReferenceField();
                } else {
                    masterDtoClassInfoDtoField =  dtoClassInfo.getIdReferenceField();
                }
            } else {
                subDtoClassInfoDtoField = subDtoClassInfo.getDtoField(dtoClassInfo.getEntityClassInfo().getTableInfo().getKeyProperty());
                masterDtoClassInfoDtoField = dtoClassInfo.getDtoField(subDtoClassInfo.getEntityClassInfo().getTableInfo().getKeyProperty());
            }
            if(subDtoClassInfoDtoField == null && masterDtoClassInfoDtoField == null) {
                continue;
            }
            queryWrapper.clear();
            if(subDtoClassInfoDtoField != null) {
                queryWrapper.eq(subDtoClassInfoDtoField.getEntityFieldInfo().getColumnName(), dtoId);
            } else {
                Object subDtoId = FieldUtils.getFieldValue(dto, masterDtoClassInfoDtoField.getEntityFieldInfo().getPropertyDescriptor().getReadMethod());
                queryWrapper.eq(subDtoClassInfo.getEntityClassInfo().getTableInfo().getKeyColumn(), subDtoId);
            }
            IService service = subDtoClassInfo.getServiceBean();
            List searchResult = service.list(queryWrapper);
            assignDtoField(dtoClassInfo.getDtoClassInfoHelper(), dto, dtoField, searchResult);
        }
    }

    public static void queryAndAssignDtoFieldBySelector(DtoClassInfoHelper dtoClassInfoHelper, EntityDtoServiceRelation relationInfo, Object dto
            , List<DtoField> dtoFields) {
        QueryWrapper queryWrapper = new QueryWrapper();
        for(DtoField dtoField : dtoFields) {
            EntityDtoServiceRelation subRelation = dtoField.getEntityDtoServiceRelation();
            if(subRelation == null) {
                continue;
            }
            DtoClassInfo subDtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(subRelation);
            IService service =  subDtoClassInfo.getServiceBean();
            queryWrapper.clear();

            dtoField.getSelectors().forEach(x -> x.connector().consume(x.tableColumn(), queryWrapper
                    , SPELParser.parseExpression(dto, Arrays.asList(x.valueExpression()))));

            List searchResult = service.list(queryWrapper);
            assignDtoField(dtoClassInfoHelper, dto, dtoField, searchResult);
        }
    }

}
