package com.circustar.mybatis_accessor.classInfo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.common_utils.parser.SPELParser;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.*;

public class DtoFields {
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
        try {
            Collection c = supportGenericType.getTargetClass().newInstance();
            for (Object var0 : values) {
                c.add(dtoClassInfoHelper.convertFromEntity(var0, dtoField.getFieldDtoClassInfo()));
            }
            FieldUtils.setFieldValue(obj, dtoField.getPropertyDescriptor().getWriteMethod(), c);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void queryAndAssignDtoFieldById(DtoClassInfo dtoClassInfo
            , EntityDtoServiceRelation relationInfo
            , List<DtoField> dtoFields
            , Object dto
            , Serializable dtoId) {
        for(DtoField dtoField : dtoFields) {
            EntityDtoServiceRelation childInfo = dtoField.getEntityDtoServiceRelation();
            if(childInfo == null) {
                continue;
            }
            DtoClassInfo subDtoClassInfo = dtoClassInfo.getDtoClassInfoHelper().getDtoClassInfo(childInfo);
            DtoField subDtoClassInfoDtoField = null;
            DtoField masterDtoClassInfoDtoField = null;
            if(subDtoClassInfo == dtoClassInfo && dtoClassInfo.getIdReferenceField() != null) {
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
            QueryWrapper qw = null;
            if(subDtoClassInfoDtoField != null) {
                qw = new QueryWrapper();
                qw.eq(subDtoClassInfoDtoField.getEntityFieldInfo().getColumnName(), dtoId);
            } else if(masterDtoClassInfoDtoField != null) {
                qw = new QueryWrapper();
                Object subDtoId = FieldUtils.getFieldValue(dto, masterDtoClassInfoDtoField.getEntityFieldInfo().getPropertyDescriptor().getReadMethod());
                qw.eq(subDtoClassInfo.getEntityClassInfo().getTableInfo().getKeyColumn(), subDtoId);
            }
            IService service = subDtoClassInfo.getServiceBean();
            List searchResult = service.list(qw);
            assignDtoField(dtoClassInfo.getDtoClassInfoHelper(), dto, dtoField, searchResult);
        }
    }

    public static void queryAndAssignDtoFieldBySelector(DtoClassInfoHelper dtoClassInfoHelper, EntityDtoServiceRelation relationInfo, Object dto
            , List<DtoField> dtoFields) {
        for(DtoField dtoField : dtoFields) {
            EntityDtoServiceRelation subRelation = dtoField.getEntityDtoServiceRelation();
            if(subRelation == null) {
                continue;
            }
            DtoClassInfo subDtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(subRelation);
            IService service =  subDtoClassInfo.getServiceBean();
            QueryWrapper qw = new QueryWrapper();

            dtoField.getSelectors().forEach(x -> x.connector().consume(x.tableColumn(), qw
                    , SPELParser.parseExpression(dto, Arrays.asList(x.valueExpression()))));

            List searchResult = service.list(qw);
            assignDtoField(dtoClassInfoHelper, dto, dtoField, searchResult);
        }
    }

}
