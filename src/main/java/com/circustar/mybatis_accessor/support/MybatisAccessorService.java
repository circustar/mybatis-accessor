package com.circustar.mybatis_accessor.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mybatis_accessor.common.MessageProperties;
import com.circustar.mybatis_accessor.provider.DefaultDeleteByIdProcessorProvider;
import com.circustar.mybatis_accessor.provider.DefaultInsertProcessorProvider;
import com.circustar.mybatis_accessor.provider.DefaultUpdateProcessorProvider;
import com.circustar.mybatis_accessor.provider.IUpdateProcessorProvider;
import com.circustar.mybatis_accessor.provider.parameter.DefaultEntityProviderParam;
import com.circustar.mybatis_accessor.provider.parameter.IProviderParam;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.mybatis_accessor.response.PageInfo;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.service.IUpdateService;
import com.circustar.common_utils.reflection.FieldUtils;

import java.io.Serializable;
import java.util.*;

public class MybatisAccessorService {
    protected IUpdateService updateService;
    protected ISelectService selectService;
    protected IEntityDtoServiceRelationMap entityDtoServiceRelationMap;
    protected DefaultInsertProcessorProvider defaultInsertProcessorProvider;
    protected DefaultDeleteByIdProcessorProvider defaultDeleteByIdProcessorProvider;
    protected DefaultUpdateProcessorProvider defaultUpdateProcessorProvider;

    public MybatisAccessorService(IEntityDtoServiceRelationMap entityDtoServiceRelationMap
            , ISelectService selectService, IUpdateService updateService
            , DefaultInsertProcessorProvider defaultInsertProcessorProvider
            , DefaultUpdateProcessorProvider defaultUpdateProcessorProvider
            , DefaultDeleteByIdProcessorProvider defaultDeleteByIdProcessorProvider) {
        this.updateService = updateService;
        this.selectService = selectService;
        this.entityDtoServiceRelationMap = entityDtoServiceRelationMap;
        this.defaultInsertProcessorProvider = defaultInsertProcessorProvider;
        this.defaultUpdateProcessorProvider = defaultUpdateProcessorProvider;
        this.defaultDeleteByIdProcessorProvider = defaultDeleteByIdProcessorProvider;
    }

    private EntityDtoServiceRelation getEntityDtoServiceRelation(Class dtoClass, String dtoName) {
        EntityDtoServiceRelation relationInfo;
        if(dtoClass != null) {
            relationInfo = this.entityDtoServiceRelationMap.getByDtoClass(dtoClass);
        } else {
            String dtoClassName = FieldUtils.parseClassName(dtoName);
            relationInfo = this.entityDtoServiceRelationMap.getByDtoName(dtoClassName);
        }
        return relationInfo;
    }

    public EntityDtoServiceRelation getRelation(Class dtoClass, String dtoName) {
        EntityDtoServiceRelation relationInfo = getEntityDtoServiceRelation(dtoClass, dtoName);
        if (relationInfo == null) {
            throw new RuntimeException(String.format(MessageProperties.DTO_NAME_NOT_FOUND, dtoName));
        }
        return relationInfo;
    }

    public <T> T getEntityById(Class dtoClass
            , Serializable id)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoClass, null);
        return this.getEntityById(relationInfo, id);
    }

    public <T> T getEntityById(String dtoName
            , Serializable id)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(null, dtoName);
        return this.getEntityById(relationInfo, id);
    }

    public <T> T getEntityById(EntityDtoServiceRelation relationInfo
            , Serializable id)  {
        return this.selectService.getEntityById(relationInfo, id);
    }

    public <T> T getEntityByQueryWrapper(Object dto
            , QueryWrapper queryWrapper)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dto.getClass(), null);
        return this.getEntityByQueryWrapper(relationInfo, dto, queryWrapper);
    }

    public <T> T getEntityByQueryWrapper(EntityDtoServiceRelation relationInfo, Object dto
            , QueryWrapper queryWrapper)  {
        return this.selectService.getEntityByQueryWrapper(relationInfo, dto, queryWrapper);
    }

    public <T> T getEntityByAnnotation(Object object)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getEntityByAnnotation(relationInfo, object);
    }

    public <T> T getEntityByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object)  {
        return this.selectService.getEntityByAnnotation(relationInfo, object);
    }

    public <T> T getDtoById(String dtoName
            , Serializable id
            , boolean includeAllChildren
            , List<String> children)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(null, dtoName);
        return this.getDtoById(relationInfo, id, includeAllChildren, children);
    }

    public <T> T getDtoById(Class dtoClass
            , Serializable id
            , boolean includeAllChildren
            , List<String> children)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoClass, null);
        return this.getDtoById(relationInfo, id, includeAllChildren, children);
    }

    public <T> T getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , boolean includeAllChildren
            , List<String> children)  {
        return this.selectService.getDtoById(relationInfo, id, includeAllChildren, children);
    }

    public <T> T getDtoByQueryWrapper(Object dto
            , QueryWrapper queryWrapper
            , boolean includeAllChildren
            , List<String> children)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dto.getClass(), null);
        return this.getDtoByQueryWrapper(relationInfo, dto, queryWrapper, includeAllChildren, children);
    }

    public <T> T getDtoByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto
            , QueryWrapper queryWrapper
            , boolean includeAllChildren
            , List<String> children)  {
        return this.selectService.getDtoByQueryWrapper(relationInfo, dto, queryWrapper, includeAllChildren, children);
    }

    public <T> T getDtoByAnnotation(Object object
            , boolean includeAllChildren
            , List<String> children)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getDtoByAnnotation(relationInfo, object, includeAllChildren, children);
    }

    public <T> T getDtoByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , boolean includeAllChildren
            , List<String> children)  {
        return this.selectService.getDtoByAnnotation(relationInfo, object, includeAllChildren, children);
    }

    public <T> PageInfo<T> getEntityPageByAnnotation(Object object
            , Integer page_index
            , Integer page_size
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getEntityPageByAnnotation(relationInfo, object, page_index, page_size);
    }

    public <T> PageInfo<T> getEntityPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
    )  {
        return this.selectService.getEntityPageByAnnotation(relationInfo
                , object
                , page_index, page_size);
    }

    public <T> PageInfo<T> getEntityPageByQueryWrapper(Object dto
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dto.getClass(), null);
        return this.getEntityPageByQueryWrapper(relationInfo,dto,queryWrapper,page_index,page_size);
    }

    public <T> PageInfo<T> getEntityPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object object
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    )  {
        return this.selectService.getEntityPageByQueryWrapper(relationInfo,object,queryWrapper,page_index,page_size);
    }

    public <T> PageInfo<T> getDtoPageByAnnotation(Object object
            , Integer page_index
            , Integer page_size
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getDtoPageByAnnotation(relationInfo, object, page_index, page_size);
    }

    public <T> PageInfo<T> getDtoPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
    )  {
        return this.selectService.getDtoPageByAnnotation(relationInfo
                , object
                , page_index, page_size);
    }

    public <T> PageInfo<T> getDtoPageByQueryWrapper(Object object
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getDtoPageByQueryWrapper(relationInfo,object,queryWrapper,page_index,page_size);
    }

    public <T> PageInfo<T> getDtoPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object object
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    )  {
        return this.selectService.getDtoPageByQueryWrapper(relationInfo,object,queryWrapper,page_index,page_size);
    }

    public List getEntityListByAnnotation(Object object
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getEntityListByAnnotation(relationInfo, object);
    }

    public List getEntityListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    )  {
        return this.selectService.getEntityListByAnnotation(relationInfo
                , object);
    }

    public <T> List<T> getEntityListByQueryWrapper(Object object
            , QueryWrapper queryWrapper
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getEntityListByQueryWrapper(relationInfo, object, queryWrapper);
    }

    public <T> List<T> getEntityListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object object
            , QueryWrapper queryWrapper
    )  {
        return this.selectService.getEntityListByQueryWrapper(relationInfo, object, queryWrapper);
    }

    public List getDtoListByAnnotation(Object object
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getDtoListByAnnotation(relationInfo, object);
    }

    public List getDtoListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    )  {
        return this.selectService.getDtoListByAnnotation(relationInfo, object);
    }

    public <T> List<T> getDtoListByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoClass, null);
        return this.getDtoListByQueryWrapper(relationInfo, null, queryWrapper);
    }

    public <T> List<T> getDtoListByQueryWrapper(Object object
            , QueryWrapper queryWrapper
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getDtoListByQueryWrapper(relationInfo, object, queryWrapper);
    }

    public <T> List<T> getDtoListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object object
            , QueryWrapper queryWrapper
    )  {
        return this.selectService.getDtoListByQueryWrapper(relationInfo, object, queryWrapper);
    }

    public <T> List<T> updateWithOptions(
            Object object, EntityDtoServiceRelation relationInfo
            , IUpdateProcessorProvider updateEntityProvider
            , IProviderParam options
            , String updateEventLogId)  {

        List<T> updatedEntities = updateService.updateByProviders(relationInfo
                , object, updateEntityProvider, options, updateEventLogId);

        return updatedEntities;
    }


    public <T> T save(Object object
            , boolean includeAllChildren
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.save(relationInfo, object, includeAllChildren, children, updateChildrenOnly, updateEventLogId);
    }

    public <T> T save(EntityDtoServiceRelation relation
            , Object object
            , boolean includeAllChildren
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId)  {
        if(object == null) {
            return null;
        }
        IProviderParam providerParam = new DefaultEntityProviderParam(updateChildrenOnly, includeAllChildren, children);
        List<T> objects = updateWithOptions(object, relation, defaultInsertProcessorProvider
                , providerParam, updateEventLogId);
        return objects.get(0);
    }


    public <T> List<T> saveList(List objects
            , boolean includeAllChildren
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId)  {
        if(objects.isEmpty()) {
            return null;
        }
        EntityDtoServiceRelation relationInfo = this.getRelation(objects.get(0).getClass(), null);
        return this.saveList(relationInfo, objects, includeAllChildren, children, updateChildrenOnly, updateEventLogId);
    }


    public <T> List<T> saveList(EntityDtoServiceRelation relation
            , List objectList
            , boolean includeAllChildren
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId)  {
        if(objectList == null || objectList.isEmpty()) {
            return null;
        }
        IProviderParam providerParam = new DefaultEntityProviderParam(updateChildrenOnly, includeAllChildren, children);
        return updateWithOptions(objectList, relation, defaultInsertProcessorProvider
                , providerParam, updateEventLogId);
    }

    public <T> T update(Object object
            , boolean includeAllChildren
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.update(relationInfo, object
                , includeAllChildren,  children, updateChildrenOnly, updateEventLogId);
    }


    public <T> T update(EntityDtoServiceRelation relation
            , Object object
            , boolean includeAllChildren
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId)  {
        IProviderParam providerParam = new DefaultEntityProviderParam(updateChildrenOnly, includeAllChildren, children);
        List<T> result = updateWithOptions(object, relation, defaultUpdateProcessorProvider
                , providerParam, updateEventLogId);
        return result.get(0);
    }


    public <T> List<T> updateList(List objects
            , boolean includeAllChildren
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId)  {
        if(objects == null || objects.isEmpty()) {
            return null;
        }
        EntityDtoServiceRelation relationInfo = this.getRelation(objects.get(0).getClass(), null);
        return this.updateList(relationInfo, objects, includeAllChildren, children
                , updateChildrenOnly, updateEventLogId);
    }


    public <T> List<T> updateList(EntityDtoServiceRelation relation, List objectList
            , boolean includeAllChildren
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId)  {
        if(objectList == null || objectList.isEmpty()) {
            return null;
        }

        IProviderParam providerParam = new DefaultEntityProviderParam(updateChildrenOnly, includeAllChildren, children);
        return updateWithOptions(objectList, relation, defaultUpdateProcessorProvider
                , providerParam, updateEventLogId);
    }

    public <T> List<T> deleteByIds(Class dtoClass
            , Set<Serializable> ids
            , boolean includeAllChildren
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoClass, null);
        return deleteByIds(relationInfo, ids, includeAllChildren, children, updateChildrenOnly, updateEventLogId);
    }

    public <T> List<T> deleteByIds(String dtoName
            , Set<Serializable> ids
            , boolean includeAllChildren
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) {
        EntityDtoServiceRelation relationInfo = this.getRelation(null, dtoName);
        return deleteByIds(relationInfo, ids, includeAllChildren, children, updateChildrenOnly, updateEventLogId);
    }

    public <T> List<T> deleteByIds(EntityDtoServiceRelation relationInfo
            , Set<Serializable> ids
            , boolean includeAllChildren
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) {
        IProviderParam providerParam = new DefaultEntityProviderParam(updateChildrenOnly, includeAllChildren, children);
        return updateWithOptions(ids, relationInfo, defaultDeleteByIdProcessorProvider
                , providerParam, updateEventLogId);
    }

    public Integer getCountByAnnotation(Object object
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getCountByAnnotation(relationInfo, object);
    }

    public Integer getCountByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    )  {
        return this.selectService.getCountByAnnotation(relationInfo
                , object);
    }

    public Integer getCountByQueryWrapper(Object object
            , QueryWrapper queryWrapper
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getCountByQueryWrapper(relationInfo, object, queryWrapper);
    }

    public Integer getCountByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object object
            , QueryWrapper queryWrapper
    )  {
        return this.selectService.getCountByQueryWrapper(relationInfo, object, queryWrapper);
    }
}
