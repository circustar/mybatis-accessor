package com.circustar.mvcenhance.classInfo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.annotation.Selector;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.utils.FieldUtils;
import com.circustar.mvcenhance.utils.SPELParser;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

public class DtoFields {
    public static List<DtoFieldInfo> getDtoFieldInfoList(IEntityDtoServiceRelationMap relationMap, EntityDtoServiceRelation e) {
        List<DtoFieldInfo> result = new ArrayList<DtoFieldInfo>();

        Field[] fields = e.getDtoClass().getDeclaredFields();
        for(Field f : fields) {
            DtoFieldInfo dtoFieldInfo = new DtoFieldInfo(e, f.getName(), null);
            EntityDtoServiceRelation dtoClass = relationMap.getByDtoClass((Class) dtoFieldInfo.getFieldInfo().getActualType());
            if(dtoClass != null) {
                result.add(dtoFieldInfo);
            }
        }

        return result;
    }

    public static List<DtoFieldInfo> getDtoFieldInfoList(IEntityDtoServiceRelationMap relationMap, EntityDtoServiceRelation e, List<String> childList) {
        List<DtoFieldInfo> result = new ArrayList<DtoFieldInfo>();
        for(String eName : childList) {
            DtoFieldInfo child = getDtoFieldInfo(relationMap, e, eName);
            if(child != null) {
                result.add(child);
            }
        }
        return result;
    }

    public static DtoFieldInfo getDtoFieldInfo(IEntityDtoServiceRelationMap relationMap, EntityDtoServiceRelation e, String eName) {
        EntityDtoServiceRelation child = relationMap.getByDtoName(eName);
        DtoFieldInfo dtoFieldInfo = null;
        if(child == null) {
            dtoFieldInfo = new DtoFieldInfo(e, eName, null);
        } else {
            dtoFieldInfo = new DtoFieldInfo(e, eName, child.getDtoClass());
        }
        if(dtoFieldInfo.getFieldInfo() != null) {
            return dtoFieldInfo;
        }
        return null;
    }

    public static void assignDtoField(DtoClassInfoHelper dtoClassInfoHelper, Object obj, DtoFieldInfo dtoFieldInfo, List<Object> values, Class clazz) throws InstantiationException, IllegalAccessException {
        if(!dtoFieldInfo.getFieldInfo().getIsCollection()) {
            FieldUtils.setField(obj, dtoFieldInfo.getFieldInfo().getField(), (values == null || values.size() == 0)?
                    null : (clazz == null? values.get(0) : dtoClassInfoHelper.convertFromEntity(values.get(0), clazz)));
            return;
        }
        DtoFieldInfo.SupportGenericType supportGenericType = DtoFieldInfo.SupportGenericType.getSupportGenericType((Class) dtoFieldInfo.getFieldInfo().getOwnType());
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
        dtoFieldInfo.getFieldInfo().getField().setAccessible(true);
        dtoFieldInfo.getFieldInfo().getField().set(obj, c);
    }

    public static void queryAndAssignDtoField(ApplicationContext applicationContext
            , DtoClassInfoHelper dtoClassInfoHelper, IEntityDtoServiceRelationMap relationMap
            , EntityDtoServiceRelation relationInfo
            , List<String> subDtoNameList
            , Object dto
            , Serializable dtoId) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        List<DtoFieldInfo> dtoFieldInfoList = DtoFields.getDtoFieldInfoList(relationMap, relationInfo, subDtoNameList);
        DtoClassInfo masterDtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        for(DtoFieldInfo dtoFieldInfo : dtoFieldInfoList) {
            EntityDtoServiceRelation childInfo = relationMap.getByDtoClass((Class)dtoFieldInfo.getFieldInfo().getActualType());
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
                Object subDtoId = FieldUtils.getValue(dto, masterDtoClassInfoDtoField.getFieldTypeInfo().getField());
                qw.eq(subDtoClassInfo.getEntityClassInfo().getTableInfo().getKeyColumn(), subDtoId);
            }
            //IService service = applicationContext.getBean(childInfo.getServiceClass());
            IService service = childInfo.getServiceBean(applicationContext);
            List searchResult = service.list(qw);
            assignDtoField(dtoClassInfoHelper, dto, dtoFieldInfo, searchResult, childInfo.getDtoClass());
        }
    }

    public static void queryAndAssignDtoField(ApplicationContext applicationContext, DtoClassInfoHelper dtoClassInfoHelper, IEntityDtoServiceRelationMap relationMap, EntityDtoServiceRelation relationInfo, Object dto
            , Map<String , Selector[]> tableJoinerMap, String groupName) throws IllegalAccessException, InstantiationException {
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext(dto);
        for(String fieldName : tableJoinerMap.keySet()) {
            Selector[] selectors = tableJoinerMap.get(fieldName);
            if(selectors == null) {
                continue;
            }
            DtoFieldInfo dtoFieldInfo = DtoFields.getDtoFieldInfo(relationMap, relationInfo, fieldName);
            EntityDtoServiceRelation subRelation = relationMap.getByDtoClass((Class) dtoFieldInfo.getFieldInfo().getActualType());
            //IService service = (IService)applicationContext.getBean(subRelation.getServiceClass());
            IService service = subRelation.getServiceBean(applicationContext);
            QueryWrapper qw = new QueryWrapper();

            Arrays.stream(selectors).forEach(x -> x.connector().consume(x.masterTableColumn(), qw
                    , SPELParser.parseExpression(standardEvaluationContext, Arrays.asList(x.valueExpression()))));

            List searchResult = service.list(qw);
            assignDtoField(dtoClassInfoHelper, dto, dtoFieldInfo, searchResult, subRelation.getDtoClass());
        }
    }

}
