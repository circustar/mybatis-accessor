package com.circustar.mvcenhance.enhance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.enhance.field.SubFieldInfo;
import com.circustar.mvcenhance.enhance.utils.FieldUtils;
import com.circustar.mvcenhance.enhance.utils.MybatisPlusUtils;
import com.circustar.mvcenhance.enhance.update.CascadeUpdateStrategy;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.enhance.update.UpdateEntity;
import com.circustar.mvcenhance.enhance.utils.EnhancedConversionService;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

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
    public <T> boolean deleteByIds(EntityDtoServiceRelation relation, String idName, List<Serializable> ids, boolean physic, boolean cascade) throws Exception {
        for(Serializable id: ids) {
            boolean success = deleteById(relation, idName, id, physic, cascade);
            if(!success) {
                throw new RuntimeException("更新错误");
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> boolean deleteById(EntityDtoServiceRelation relation, String idName, Serializable id, boolean physic, boolean cascade) throws Exception {
        if(cascade) {
            //删除子项
            List<SubFieldInfo> subDtoList = SubFieldInfo.getSubFieldInfoList(entityDtoServiceRelationMap, relation);
            for(SubFieldInfo sub : subDtoList) {
                EntityDtoServiceRelation dtoRelation = entityDtoServiceRelationMap.getByDtoClass((Class)sub.getFieldInfo().getActualType());
                IService dtoService = (IService)applicationContext.getBean(dtoRelation.getService());
                TableInfo subTableInfo = TableInfoHelper.getTableInfo(dtoRelation.getEntity());
                if(subTableInfo.getFieldList().stream().filter(x -> x.getColumn().equals(idName)).count() == 0) {
                    continue;
                }
                QueryWrapper qw = new QueryWrapper();
                qw.eq(idName, id);
                MybatisPlusUtils.delete(dtoService, qw, physic);
            }
        }
        IService service = (IService)applicationContext.getBean(relation.getService());
        //删除主项
        return MybatisPlusUtils.deleteById(service, id, physic);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> boolean save(EntityDtoServiceRelation relation, TableInfo tableInfo, Object dto) throws Exception {
        //主表
        IService service = applicationContext.getBean(relation.getService());
        Object entity = converter.convert(dto, relation.getEntity());
        Boolean result = service.save(entity);
        if(!result) {
            throw new RuntimeException("更新错误");
        }
        Object idValue = FieldUtils.getValueByName(entity, tableInfo.getKeyProperty());

        //子项列表
        List<SubFieldInfo> subDtoList = SubFieldInfo.getSubFieldInfoList(entityDtoServiceRelationMap, relation);
        for(SubFieldInfo sub : subDtoList) {
            sub.getFieldInfo().getField().setAccessible(true);
            Object subObj = sub.getFieldInfo().getField().get(dto);
            if(subObj == null) {
                continue;
            }
            EntityDtoServiceRelation subRelation = entityDtoServiceRelationMap.getByDtoClass((Class)sub.getFieldInfo().getActualType());
            IService subService = applicationContext.getBean(subRelation.getService());
            if(sub.getFieldInfo().getIsCollection()) {
                List subEntities = converter.convertCollection((Collection)subObj, subRelation.getEntity());
                for(Object o : subEntities) {
                    FieldUtils.setFieldByName(o, tableInfo.getKeyProperty(), idValue);
                }
                result = subService.saveBatch(subEntities);
                if(!result) {
                    throw new RuntimeException("更新错误");
                }
            } else {
                Object subEntity = converter.convert(subObj, subRelation.getEntity());
                FieldUtils.setFieldByName(subEntity, tableInfo.getKeyProperty(), idValue);
                result = subService.save(subEntity);
                if(!result) {
                    throw new RuntimeException("更新错误");
                }
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> boolean update(EntityDtoServiceRelation relation, TableInfo tableInfo, Serializable idValue, Object dto, CascadeUpdateStrategy updateStrategy, List<String> assignedTargetList) throws Exception {
        //主表
        IService service = applicationContext.getBean(relation.getService());
        Object entity = converter.convert(dto, relation.getEntity());
        Object idTypeValue = converter.convert(idValue, tableInfo.getKeyType());
        FieldUtils.setFieldByName(entity, tableInfo.getKeyProperty(), idTypeValue);
        Boolean result = service.updateById(entity);
        if(!result) {
            throw new RuntimeException("更新错误");
        }

        if(updateStrategy == CascadeUpdateStrategy.NONE) {
            return result;
        }
        //子项列表
        List<SubFieldInfo> subDtoList = SubFieldInfo.getSubFieldInfoList(entityDtoServiceRelationMap, relation);

        for(SubFieldInfo sub : subDtoList) {
            QueryWrapper qw = new QueryWrapper();
            qw.eq(tableInfo.getKeyColumn(), idValue);
            EntityDtoServiceRelation subRelation = entityDtoServiceRelationMap.getByDtoClass((Class)sub.getFieldInfo().getActualType());
            IService subService = applicationContext.getBean(subRelation.getService());
            if(updateStrategy == CascadeUpdateStrategy.REMOVE_ALL
                    || updateStrategy == CascadeUpdateStrategy.PHYSIC_REMOVE_ALL) {
                MybatisPlusUtils.delete(subService, qw, updateStrategy == CascadeUpdateStrategy.PHYSIC_REMOVE_ALL);
            } if(updateStrategy == CascadeUpdateStrategy.REMOVE_IGNORE_NULL
                    || updateStrategy == CascadeUpdateStrategy.PHYSIC_REMOVE_IGNORE_NULL) {
                Object subObj = sub.getFieldInfo().getField().get(dto);
                if(subObj != null) {
                    MybatisPlusUtils.delete(subService, qw, updateStrategy == CascadeUpdateStrategy.PHYSIC_REMOVE_IGNORE_NULL);
                }
            } else if(updateStrategy == CascadeUpdateStrategy.REMOVE_ASSIGN
                    || updateStrategy == CascadeUpdateStrategy.PHYSIC_REMOVE_ASSIGN) {
                if(assignedTargetList.contains(sub.getFieldInfo().getField().getName())) {
                    MybatisPlusUtils.delete(subService, qw, updateStrategy == CascadeUpdateStrategy.PHYSIC_REMOVE_ASSIGN);
                }
            }
            sub.getFieldInfo().getField().setAccessible(true);
            Object subObj = sub.getFieldInfo().getField().get(dto);
            if(subObj == null) {
                continue;
            }
            TableInfo subTableInfo = TableInfoHelper.getTableInfo(subRelation.getEntity());
            if(subTableInfo.getFieldList().stream().filter(x -> x.getColumn().equals(tableInfo.getKeyColumn())).count() == 0) {
                continue;
            }
            if(sub.getFieldInfo().getIsCollection()) {
                List subEntities = converter.convertCollection((Collection)subObj, subRelation.getEntity());
                for(Object o : subEntities) {
                    FieldUtils.setFieldByName(o, tableInfo.getKeyProperty(), idTypeValue);
                }
                result = subService.saveBatch(subEntities);
                if(!result) {
                    throw new RuntimeException("更新错误");
                }
            } else {
                Object subEntity = converter.convert(subObj, subRelation.getEntity());
                FieldUtils.setFieldByName(subEntity, tableInfo.getKeyProperty(), idTypeValue);
                result = subService.save(subEntity);
                if(!result) {
                    throw new RuntimeException("更新错误");
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> boolean updateStatus(List<UpdateEntity> objList) throws Exception {
        boolean result = false;
        for(UpdateEntity o : objList) {
            IService service = applicationContext.getBean(o.getRelation().getService());
            result = o.execUpdate(service);
            if(!result) {
                throw new RuntimeException("更新错误");
            }
        }
        return result;
    }
}
