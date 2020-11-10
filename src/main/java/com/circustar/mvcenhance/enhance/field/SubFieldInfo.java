package com.circustar.mvcenhance.enhance.field;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.common.query.EntityFilter;
import com.circustar.mvcenhance.enhance.utils.SPELParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.enhance.utils.EnhancedConversionService;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

public class SubFieldInfo {
    private EntityDtoServiceRelation relationInfo;
    private String subDtoName;
    private Class subDtoClass;

    public EntityDtoServiceRelation getRelationInfo() {
        return relationInfo;
    }

    public void setRelationInfo(EntityDtoServiceRelation relationInfo) {
        this.relationInfo = relationInfo;
    }

    public String getSubDtoName() {
        return subDtoName;
    }

    public void setSubDtoName(String subDtoName) {
        this.subDtoName = subDtoName;
    }

    public Class getSubDtoClass() {
        return subDtoClass;
    }

    public void setSubDtoClass(Class subDtoClass) {
        this.subDtoClass = subDtoClass;
    }

    public ParsedFieldInfo getFieldInfo() {
        return fieldInfo;
    }

    public void setFieldInfo(ParsedFieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    private ParsedFieldInfo fieldInfo;
    public SubFieldInfo(EntityDtoServiceRelation relationInfo, String subDtoName, Class subDtoClass) {
        this.relationInfo = relationInfo;
        this.subDtoName = subDtoName;
        this.subDtoClass = subDtoClass;

        fieldInfo = this.parseField();
//        if(fieldInfo == null) {
//            return;
//        }
    }
    public static List<SubFieldInfo> getSubFieldInfoList(IEntityDtoServiceRelationMap relationMap, EntityDtoServiceRelation e) {
        List<SubFieldInfo> result = new ArrayList<SubFieldInfo>();

        Field[] fields = e.getDto().getDeclaredFields();
        for(Field f : fields) {
            SubFieldInfo subFieldInfo = new SubFieldInfo(e, f.getName(), null);
            EntityDtoServiceRelation dtoClass = relationMap.getByDtoClass((Class) subFieldInfo.getFieldInfo().getActualType());
            if(dtoClass != null) {
                result.add(subFieldInfo);
            }
        }

        return result;
    }

    public static List<SubFieldInfo> getSubFieldInfoList(IEntityDtoServiceRelationMap relationMap, EntityDtoServiceRelation e, List<String> subEntityList) {
        List<SubFieldInfo> result = new ArrayList<SubFieldInfo>();
        for(String eName : subEntityList) {
            SubFieldInfo subEntity = getSubFieldInfo(relationMap, e, eName);
            //SubDtoInfo subDtoInfo = new SubDtoInfo(e, eName, subEntity.getDto());
            if(subEntity != null) {
                result.add(subEntity);
            }
        }
        return result;
    }

    public static SubFieldInfo getSubFieldInfo(IEntityDtoServiceRelationMap relationMap, EntityDtoServiceRelation e, String eName) {
        EntityDtoServiceRelation subEntity = relationMap.getByDtoName(eName);
        SubFieldInfo subFieldInfo = null;
        if(subEntity == null) {
            subFieldInfo = new SubFieldInfo(e, eName, null);
        } else {
            subFieldInfo = new SubFieldInfo(e, eName, subEntity.getDto());
        }
        if(subFieldInfo.getFieldInfo() != null) {
            return subFieldInfo;
        }
        return null;
    }

    public static void setListBySubDtoInfo(EnhancedConversionService converter, Object obj, SubFieldInfo subFieldInfo, List<Object> values, Class clazz) throws InstantiationException, IllegalAccessException {
        if(!subFieldInfo.getFieldInfo().getIsCollection()) {
            subFieldInfo.getFieldInfo().getField().set(obj, (values == null || values.size() == 0)?
                    null : (clazz == null? values.get(0) : converter.convert(values.get(0), clazz)));
            return;
        }
        SupportGenericType supportGenericType = SupportGenericType.getSupportGenericType((Class) subFieldInfo.getFieldInfo().getOwnType());
        if(supportGenericType == null) {
            return;
        }
        Collection c = supportGenericType.getTargetClass().newInstance();
        for(Object var0 : values) {
            if(clazz == null) {
                c.add(var0);
                continue;
            }
            c.add(converter.convert(var0, clazz));
        }
//        values.stream().map(x -> {
//            if(clazz == null) {
//                return x;
//            }
//            return converter.convert(x, clazz);
//        }).forEach(c::add);
        subFieldInfo.getFieldInfo().getField().setAccessible(true);
        subFieldInfo.getFieldInfo().getField().set(obj, c);
    }

    public static void setSubDtoAfterQueryById(ApplicationContext applicationContext
            , EnhancedConversionService converter, IEntityDtoServiceRelationMap relationMap
            , EntityDtoServiceRelation relationInfo, Object dto
            , List<String> subDtoNameList
            , String idName, Serializable idValue) throws IllegalAccessException, InstantiationException {
        List<SubFieldInfo> subFieldInfoList = SubFieldInfo.getSubFieldInfoList(relationMap, relationInfo, subDtoNameList);
        for(SubFieldInfo x : subFieldInfoList) {
            EntityDtoServiceRelation subEntityInfo = relationMap.getByDtoClass((Class)x.getFieldInfo().getActualType());
            if(subEntityInfo == null) {
                continue;
            }
            IService service = (IService)applicationContext.getBean(subEntityInfo.getService());
            QueryWrapper qw = new QueryWrapper();
            qw.eq(idName, idValue);
            List searchResult = service.list(qw);
            setListBySubDtoInfo(converter, dto, x, searchResult, subEntityInfo.getDto());
        }
    }

    public static void setSubDtoAfterQueryByTableJoiner(ApplicationContext applicationContext, EnhancedConversionService converter, IEntityDtoServiceRelationMap relationMap, EntityDtoServiceRelation relationInfo, Object dto
            , Map<String , EntityFilter[]> tableJoinerMap, String groupName) throws IllegalAccessException, InstantiationException {
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext(dto);
        for(String fieldName : tableJoinerMap.keySet()) {
            EntityFilter[] entityFilters = tableJoinerMap.get(fieldName);
            if(entityFilters == null) {
                continue;
            }
            SubFieldInfo subFieldInfo = SubFieldInfo.getSubFieldInfo(relationMap, relationInfo, fieldName);
            EntityDtoServiceRelation subRelation = relationMap.getByDtoClass((Class)subFieldInfo.getFieldInfo().getActualType());
            IService service = (IService)applicationContext.getBean(subRelation.getService());
            QueryWrapper qw = new QueryWrapper();
//            qw.apply("left join teacher th on th.teacher_id = 3");

            Arrays.stream(entityFilters)
                    .filter(x -> {
                        return (x.group().length == 0 && StringUtils.isEmpty(groupName))
                                || (Arrays.stream(x.group()).anyMatch(y -> y.equals(groupName)));
                    }).forEach(x -> x.connector().consume(x.column(), qw
                    , SPELParser.parseExpression(standardEvaluationContext, Arrays.asList(x.valueExpression()))));

            List searchResult = service.list(qw);
            setListBySubDtoInfo(converter, dto, subFieldInfo, searchResult, subRelation.getDto());
        }
    }

    private ParsedFieldInfo parseField() {
        ParsedFieldInfo f = null;
        if(!StringUtils.isEmpty(this.subDtoName)) {
            f = ParsedFieldInfo.parseFieldByName(relationInfo.getDto(), this.subDtoName);
            //f = PropertyUtils.findFieldByName(entityInfo.getDto(), this.subDtoName);
        }
        if(f != null || this.subDtoClass == null) {
            return f;
        }
        return ParsedFieldInfo.parseFieldByClass(relationInfo.getDto(), this.getSubDtoClass(), true);
    }

    enum SupportGenericType{
        list(List.class, ArrayList.class),
        collection(Collection.class, ArrayList.class),
        set(Set.class, HashSet.class),
        queue(Queue.class, PriorityQueue.class);
        private Class<? extends Collection> type;
        private Class<? extends Collection> newType;
        SupportGenericType(Class type, Class newType) {
            this.type = type;
            this.newType = newType;
        }
        public Class<? extends Collection> getOriginClass() {
            return this.type;
        }
        public Class<? extends Collection> getTargetClass() {
            return this.newType;
        }
        public static SupportGenericType getSupportGenericType(Class t) {
            return Arrays.stream(SupportGenericType.values()).filter(x -> x.getOriginClass() == t).findFirst().orElse(null);
        }
    }
}


