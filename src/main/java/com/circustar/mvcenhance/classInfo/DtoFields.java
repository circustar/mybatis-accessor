package com.circustar.mvcenhance.classInfo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.utils.FieldUtils;
import com.circustar.mvcenhance.utils.SPELParser;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.Serializable;
import java.util.*;

public class DtoFields {
    public static void assignDtoField(DtoClassInfoHelper dtoClassInfoHelper, Object obj, DtoField dtoField, List<Object> values, Class clazz) throws Exception {
        if(!dtoField.getCollection()) {
            FieldUtils.setField(obj, dtoField.getField(), (values == null || values.size() == 0)?
                    null : (clazz == null? values.get(0) : dtoClassInfoHelper.convertFromEntity(values.get(0), clazz)));
            return;
        }
        DtoField.SupportGenericType supportGenericType = DtoField.SupportGenericType.getSupportGenericType((Class) dtoField.getOwnType());
        if(supportGenericType == null) {
            return;
        }
        Collection c = supportGenericType.getTargetClass().newInstance();
        for(Object var0 : values) {
            if(clazz == null) {
                c.add(var0);
                continue;
            }
            c.add(dtoClassInfoHelper.convertFromEntity(var0, clazz));
        }
        dtoField.getField().setAccessible(true);
        dtoField.getField().set(obj, c);
    }

    public static void queryAndAssignDtoFieldById(ApplicationContext applicationContext
            , DtoClassInfoHelper dtoClassInfoHelper, IEntityDtoServiceRelationMap relationMap
            , EntityDtoServiceRelation relationInfo
            , List<DtoField> dtoFields
            , Object dto
            , Serializable dtoId) throws Exception {
        DtoClassInfo masterDtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        for(DtoField dtoField : dtoFields) {
            EntityDtoServiceRelation childInfo = dtoField.getEntityDtoServiceRelation();
            if(childInfo == null) {
                continue;
            }
            DtoClassInfo subDtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(childInfo.getDtoClass());
            DtoField subDtoClassInfoDtoField = subDtoClassInfo.getDtoField(masterDtoClassInfo.getEntityClassInfo().getTableInfo().getKeyProperty());
            DtoField masterDtoClassInfoDtoField = masterDtoClassInfo.getDtoField(subDtoClassInfo.getEntityClassInfo().getTableInfo().getKeyProperty());
            if(subDtoClassInfoDtoField == null && masterDtoClassInfoDtoField == null) {
                continue;
            }
            QueryWrapper qw = null;
            if(subDtoClassInfoDtoField != null) {
                qw = new QueryWrapper();
                qw.eq(masterDtoClassInfo.getEntityClassInfo().getTableInfo().getKeyColumn(), dtoId);
            } else if(masterDtoClassInfoDtoField != null) {
                qw = new QueryWrapper();
                Object subDtoId = FieldUtils.getValue(dto, masterDtoClassInfoDtoField.getEntityFieldInfo().getField());
                qw.eq(subDtoClassInfo.getEntityClassInfo().getTableInfo().getKeyColumn(), subDtoId);
            }
            IService service = childInfo.getServiceBean(applicationContext);
            List searchResult = service.list(qw);
            assignDtoField(dtoClassInfoHelper, dto, dtoField, searchResult, childInfo.getDtoClass());
        }
    }

    public static void queryAndAssignDtoFieldBySelector(ApplicationContext applicationContext, DtoClassInfoHelper dtoClassInfoHelper, IEntityDtoServiceRelationMap relationMap, EntityDtoServiceRelation relationInfo, Object dto
            , List<DtoField> dtoFields) throws Exception {
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext(dto);
        for(DtoField dtoField : dtoFields) {
            EntityDtoServiceRelation subRelation = dtoField.getEntityDtoServiceRelation();
            if(subRelation == null) {
                continue;
            }
            IService service = subRelation.getServiceBean(applicationContext);
            QueryWrapper qw = new QueryWrapper();

            Arrays.stream(dtoField.getSelectors()).forEach(x -> x.connector().consume(x.tableColumn(), qw
                    , SPELParser.parseExpression(standardEvaluationContext, Arrays.asList(x.valueExpression()))));

            List searchResult = service.list(qw);
            assignDtoField(dtoClassInfoHelper, dto, dtoField, searchResult, subRelation.getDtoClass());
        }
    }

}
