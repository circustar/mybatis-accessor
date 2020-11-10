package com.circustar.mvcenhance.enhance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.common.error.UpdateFailException;
import com.circustar.mvcenhance.enhance.field.SubFieldInfo;
import com.circustar.mvcenhance.enhance.update.DeleteField;
import com.circustar.mvcenhance.enhance.utils.AnnotationUtils;
import com.circustar.mvcenhance.enhance.utils.FieldUtils;
import com.circustar.mvcenhance.enhance.utils.MybatisPlusUtils;
import com.circustar.mvcenhance.enhance.update.UpdateSubEntityStrategy;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.enhance.update.UpdateEntity;
import com.circustar.mvcenhance.enhance.utils.EnhancedConversionService;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;

public class CrudService implements ICrudService {
    public CrudService(ApplicationContext applicationContext, EnhancedConversionService converter, IEntityDtoServiceRelationMap entityDtoServiceRelationMap) {
        this.applicationContext = applicationContext;
        this.converter = converter;
        this.entityDtoServiceRelationMap = entityDtoServiceRelationMap;
    }
    private ApplicationContext applicationContext;

    private IEntityDtoServiceRelationMap entityDtoServiceRelationMap;

    private EnhancedConversionService converter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> void deleteByIds(EntityDtoServiceRelation relation, String idName
            , List<Serializable> ids
            , List<String> subEntities, boolean physic) throws UpdateFailException {
        boolean success = false;
        IService service = (IService)applicationContext.getBean(relation.getService());
        if(subEntities != null && subEntities.size() > 0) {
            for (Serializable id : ids) {
                deleteById(relation, idName, id, subEntities, physic);
            }
            return;
        }
        success = MybatisPlusUtils.deleteBatchIds(service, ids, physic);
        if (!success) {
            throw new UpdateFailException("删除失败");
        }
        return;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> void deleteById(EntityDtoServiceRelation relation, String idName
            , Serializable id
            , List<String> subEntities, boolean physic) throws UpdateFailException {
        IService service = (IService)applicationContext.getBean(relation.getService());
        if(subEntities != null && subEntities.size() > 0) {
//            Object master = service.getById(id);
//            if(master == null) {
//                return true;
//            }
//            StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext(master);
            //删除子项
            List<SubFieldInfo> subDtoList = SubFieldInfo.getSubFieldInfoList(entityDtoServiceRelationMap, relation, subEntities);
            for (SubFieldInfo sub : subDtoList) {
                EntityDtoServiceRelation dtoRelation = entityDtoServiceRelationMap.getByDtoClass((Class) sub.getFieldInfo().getActualType());
                IService dtoService = (IService) applicationContext.getBean(dtoRelation.getService());
                TableInfo subTableInfo = TableInfoHelper.getTableInfo(dtoRelation.getEntity());
                if (subTableInfo.getFieldList().stream().filter(x -> x.getColumn().equals(idName)).findAny().isPresent()) {
                    QueryWrapper qw = new QueryWrapper();
                    qw.eq(idName, id);
                    boolean success = MybatisPlusUtils.delete(dtoService, qw, physic);
                    if (!success) {
                        throw new UpdateFailException("删除失败");
                    }
                } else {
                      // 不支持使用条件查询方式删除
//                    QueryWrapper qw = new QueryWrapper();
//                    List<EntityFilter> entityFilters = Arrays.stream(FieldUtils.getFieldAnnotationsByType(dtoRelation.getDto().getClass(), sub.getSubDtoName(), EntityFilter.class))
//                            .filter(x -> StringUtils.isEmpty(x.group())).collect(Collectors.toList());
//                    if(entityFilters.size() > 0) {
//                        entityFilters.forEach(x -> x.connector().consume(x.column(), qw
//                                , SPELParser.parseExpression(standardEvaluationContext, Arrays.asList(x.valueExpression()))));
//                        MybatisPlusUtils.delete(dtoService, qw, physic);
//                    } else {
//                        throw new Exception("无法删除指定实体类：" + sub.getSubDtoName());
//                    }
                    throw new RuntimeException("无法删除指定实体类：" + sub.getSubDtoName());
                }
            }
        }

        //删除主项
        boolean success = MybatisPlusUtils.deleteById(service, id, physic);
        if (!success) {
            throw new UpdateFailException("删除失败");
        }
        return;
    }

    private void saveSubEntity(String keyProperty, Object idValue
            , SubFieldInfo subFieldInfo, Object dto) throws IllegalAccessException, InstantiationException, NoSuchFieldException, UpdateFailException {
        boolean result = false;
        EntityDtoServiceRelation subRelation = entityDtoServiceRelationMap.getByDtoClass((Class)subFieldInfo.getFieldInfo().getActualType());
        IService subService = applicationContext.getBean(subRelation.getService());
        if(subFieldInfo.getFieldInfo().getIsCollection()) {
            Collection subEntities = converter.convertCollection((Collection)dto, subRelation.getEntity());
            for(Object o : subEntities) {
                FieldUtils.setFieldByName(o, keyProperty, idValue, converter);
            }
            result = subService.saveBatch(subEntities);
            if(!result) {
                throw new UpdateFailException("保存失败");
            }
        } else {
            Object subEntity = converter.convert(dto, subRelation.getEntity());
            FieldUtils.setFieldByName(subEntity, keyProperty, idValue, converter);
            result = subService.save(subEntity);
            if(!result) {
                throw new UpdateFailException("保存失败");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> void save(EntityDtoServiceRelation relation
            , Object dto, List<String> subEntityList) throws UpdateFailException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        //主表
        IService service = applicationContext.getBean(relation.getService());
        Object entity = converter.convert(dto, relation.getEntity());
        Boolean result = service.save(entity);
        if(!result) {
            throw new UpdateFailException("保存失败");
        }
        TableInfo tableInfo = TableInfoHelper.getTableInfo(relation.getEntity());
        Object idValue = FieldUtils.getValueByName(entity, tableInfo.getKeyProperty());
        FieldUtils.copyProperties(entity, dto);
        //子项列表
        List<SubFieldInfo> subDtoList = SubFieldInfo.getSubFieldInfoList(entityDtoServiceRelationMap, relation, subEntityList);
        for(SubFieldInfo sub : subDtoList) {
            sub.getFieldInfo().getField().setAccessible(true);
            Object subObj = sub.getFieldInfo().getField().get(dto);
            if(subObj == null) {
                continue;
            }
            saveSubEntity(tableInfo.getKeyProperty(), idValue, sub, subObj);
//            EntityDtoServiceRelation subRelation = entityDtoServiceRelationMap.getByDtoClass((Class)sub.getFieldInfo().getActualType());
//            IService subService = applicationContext.getBean(subRelation.getService());
//            if(sub.getFieldInfo().getIsCollection()) {
//                Collection subEntities = converter.convertCollection((Collection)subObj, subRelation.getEntity());
//                for(Object o : subEntities) {
//                    FieldUtils.setFieldByName(o, tableInfo.getKeyProperty(), idValue);
//                }
//                result = subService.saveBatch(subEntities);
//                if(!result) {
//                    throw new RuntimeException("保存错误");
//                }
//            } else {
//                Object subEntity = converter.convert(subObj, subRelation.getEntity());
//                FieldUtils.setFieldByName(subEntity, tableInfo.getKeyProperty(), idValue);
//                result = subService.save(subEntity);
//                if(!result) {
//                    throw new RuntimeException("保存失败");
//                }
//            }
        }

        return;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> void saveList(EntityDtoServiceRelation relation
            , List<Object> dtoList) throws UpdateFailException, IllegalAccessException, InstantiationException {
        boolean result = false;
        IService service = applicationContext.getBean(relation.getService());
        List<Object> entities = converter.convertList(dtoList, relation.getEntity());
        result = service.saveBatch(entities);
        if(!result) {
            throw new UpdateFailException("保存失败");
        }
        return;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> void saveOrUpdateOrDeleteList(EntityDtoServiceRelation relation
            , List<Object> dtoList, boolean physicDelete) throws NoSuchFieldException, IllegalAccessException, InstantiationException, UpdateFailException {
        boolean result = true;
        TableInfo tableInfo = TableInfoHelper.getTableInfo(relation.getEntity());
        String keyProperty = tableInfo.getKeyProperty();
        IService service = applicationContext.getBean(relation.getService());

        List saveOrUpdateItems = new ArrayList();
        List deleteItems = new ArrayList();

        for(Object dto : dtoList) {
            Object keyValue = FieldUtils.getValueByName(dto, keyProperty);
            if(Objects.nonNull(keyValue)) {
                String deleteFieldName = AnnotationUtils.getDeleteFieldAnnotationValue(relation.getDto());
                if(!StringUtils.isEmpty(deleteFieldName)) {
                    Object deleteValue = FieldUtils.getValueByName(dto, deleteFieldName);
                    if(Objects.nonNull(deleteValue)) {
                        deleteItems.add(keyValue);
                        continue;
                    }
                }
            }
            saveOrUpdateItems.add(converter.convert(dto, relation.getEntity()));
        }

        if(deleteItems.size() > 0) {
            if(!MybatisPlusUtils.deleteBatchIds(service, deleteItems, physicDelete)) {
                throw new UpdateFailException("删除失败");
            }
        }

        if(saveOrUpdateItems.size() > 0) {
            result = service.saveOrUpdateBatch(saveOrUpdateItems);
            if(!result) {
                throw new UpdateFailException("更新失败");
            }
        }

        return;
    }

    private void saveOrUpdateOrDeleteSubField(TableInfo tableInfo, Serializable idValue
            , Object subObj , EntityDtoServiceRelation subRelation, boolean physicDelete) throws NoSuchFieldException, IllegalAccessException, UpdateFailException, InstantiationException {
        if(subObj == null) {
            return;
        }
//        EntityDtoServiceRelation subRelation = entityDtoServiceRelationMap.getByDtoClass((Class)subFieldInfo.getFieldInfo().getActualType());
        List updateObjects;
        if(Collection.class.isAssignableFrom(subObj.getClass())) {
            updateObjects = Arrays.asList(((Collection)subObj).toArray());
        } else {
            updateObjects = Collections.singletonList(subObj);
        }
        for(Object obj : updateObjects) {
            FieldUtils.setFieldByName(obj, tableInfo.getKeyProperty(), idValue, converter);
        }
        saveOrUpdateOrDeleteList(subRelation, updateObjects, physicDelete);
    }

    private void saveOrUpdateOrDeleteSubField(Object dto, TableInfo tableInfo, Serializable idValue
            , SubFieldInfo subFieldInfo, boolean physicDelete) throws IllegalAccessException, UpdateFailException, NoSuchFieldException, InstantiationException {
        subFieldInfo.getFieldInfo().getField().setAccessible(true);
        Object subObj = subFieldInfo.getFieldInfo().getField().get(dto);
        if(subObj == null) {
            return;
        }
        EntityDtoServiceRelation subRelation = entityDtoServiceRelationMap.getByDtoClass((Class)subFieldInfo.getFieldInfo().getActualType());
        saveOrUpdateOrDeleteSubField(tableInfo, idValue, subObj, subRelation, physicDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> void update(EntityDtoServiceRelation relation
            , Serializable idValue, Object dto
            , List<String> assignedTargetList
            , UpdateSubEntityStrategy updateStrategy
            , boolean physicDelete) throws InstantiationException, IllegalAccessException, NoSuchFieldException, UpdateFailException {
        //主表
        TableInfo tableInfo = TableInfoHelper.getTableInfo(relation.getEntity());
        IService service = applicationContext.getBean(relation.getService());
        Object entity = converter.convert(dto, relation.getEntity());
        Object idTypeValue = converter.convert(idValue, tableInfo.getKeyType());
        FieldUtils.setFieldByName(entity, tableInfo.getKeyProperty(), idTypeValue, converter);
        Boolean result = service.updateById(entity);
        if(!result) {
            throw new UpdateFailException("更新失败");
        }

        //子项列表
        List<SubFieldInfo> subDtoList = SubFieldInfo.getSubFieldInfoList(entityDtoServiceRelationMap
                , relation, assignedTargetList);

        for(SubFieldInfo subFieldInfo : subDtoList) {
            if(updateStrategy == UpdateSubEntityStrategy.DELETE_BEFORE_INSERT) {
                QueryWrapper qw = new QueryWrapper();
                qw.eq(tableInfo.getKeyColumn(), idValue);
                EntityDtoServiceRelation subFieldRelation = entityDtoServiceRelationMap.getByDtoClass((Class) subFieldInfo.getFieldInfo().getActualType());
                MybatisPlusUtils.delete(applicationContext.getBean(subFieldRelation.getService())
                        , qw, physicDelete);
            }
            saveOrUpdateOrDeleteSubField(dto, tableInfo, idValue, subFieldInfo, physicDelete);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> void updateSubEntityList(EntityDtoServiceRelation relation
            , Serializable idValue, EntityDtoServiceRelation subFieldRelation
            , List<Object> subEntities, UpdateSubEntityStrategy updateStrategy
            , boolean physicDelete) throws UpdateFailException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        boolean result = true;
        //子项列表
        if(UpdateSubEntityStrategy.DELETE_BEFORE_INSERT == updateStrategy) {
            TableInfo tableInfo = TableInfoHelper.getTableInfo(relation.getEntity());

            QueryWrapper qw = new QueryWrapper();
            qw.eq(tableInfo.getKeyColumn(), idValue);
            MybatisPlusUtils.delete(applicationContext.getBean(subFieldRelation.getService())
                    , qw, physicDelete);
        }

        TableInfo subEntityTableInfo = TableInfoHelper.getTableInfo(relation.getEntity());
        saveOrUpdateOrDeleteSubField(subEntityTableInfo, idValue, subEntities, subFieldRelation, physicDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> void businessUpdate(List<UpdateEntity> objList) throws Exception {
        boolean result = false;
        for(UpdateEntity o : objList) {
            IService service = applicationContext.getBean(o.getRelation().getService());
            result = o.execUpdate(service);
            if(!result) {
                throw new UpdateFailException("更新失败");
            }
        }
    }
}
