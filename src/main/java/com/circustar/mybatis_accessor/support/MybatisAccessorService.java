package com.circustar.mybatis_accessor.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;
import com.circustar.mybatis_accessor.provider.*;
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
    protected DefaultSaveOrUpdateProcessorProvider defaultSaveOrUpdateProcessorProvider;

    public MybatisAccessorService(IEntityDtoServiceRelationMap entityDtoServiceRelationMap
            , ISelectService selectService, IUpdateService updateService
            , DefaultInsertProcessorProvider defaultInsertProcessorProvider
            , DefaultUpdateProcessorProvider defaultUpdateProcessorProvider
            , DefaultDeleteByIdProcessorProvider defaultDeleteByIdProcessorProvider
            , DefaultSaveOrUpdateProcessorProvider defaultSaveOrUpdateProcessorProvider) {
        this.updateService = updateService;
        this.selectService = selectService;
        this.entityDtoServiceRelationMap = entityDtoServiceRelationMap;
        this.defaultInsertProcessorProvider = defaultInsertProcessorProvider;
        this.defaultUpdateProcessorProvider = defaultUpdateProcessorProvider;
        this.defaultDeleteByIdProcessorProvider = defaultDeleteByIdProcessorProvider;
        this.defaultSaveOrUpdateProcessorProvider = defaultSaveOrUpdateProcessorProvider;
    }

    private EntityDtoServiceRelation getEntityDtoServiceRelation(Class dtoClass, String dtoName) {
        EntityDtoServiceRelation relationInfo;
        if(dtoClass != null) {
            relationInfo = this.entityDtoServiceRelationMap.getByDtoClass(dtoClass);
            if(relationInfo == null) {
                relationInfo = this.entityDtoServiceRelationMap.getByDtoName(dtoClass.getName());
            }
        } else {
            relationInfo = this.entityDtoServiceRelationMap.getByDtoName(dtoName);
        }
        return relationInfo;
    }

    public EntityDtoServiceRelation getRelation(Class dtoClass, String dtoName) {
        return getEntityDtoServiceRelation(dtoClass, dtoName);
    }

    public <T> T getEntityById(Class dtoClass
            , Serializable id) {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoClass, null);
        return this.getEntityById(relationInfo, id);
    }

    public <T> T getEntityById(String dtoName
            , Serializable id) {
        EntityDtoServiceRelation relationInfo = this.getRelation(null, dtoName);
        return this.getEntityById(relationInfo, id);
    }

    private <T> T getEntityById(EntityDtoServiceRelation relationInfo
            , Serializable id)  {
        return this.selectService.getEntityById(relationInfo, id);
    }

    public <T> T getEntityByQueryWrapper(Object dto
            , QueryWrapper queryWrapper) {
        EntityDtoServiceRelation relationInfo = this.getRelation(dto.getClass(), null);
        return this.getEntityByQueryWrapper(relationInfo, dto, queryWrapper);
    }

    private <T> T getEntityByQueryWrapper(EntityDtoServiceRelation relationInfo, Object dto
            , QueryWrapper queryWrapper)  {
        return this.selectService.getEntityByQueryWrapper(relationInfo, dto, queryWrapper);
    }

    public <T> T getEntityByAnnotation(Object object) {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getEntityByAnnotation(relationInfo, object);
    }

    private <T> T getEntityByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object)  {
        return this.selectService.getEntityByAnnotation(relationInfo, object);
    }

    public <T> T getDtoById(String dtoName
            , Serializable id) {
        EntityDtoServiceRelation relationInfo = this.getRelation(null, dtoName);
        return this.getDtoById(relationInfo, id, null);
    }

    public <T> T getDtoById(String dtoName
            , Serializable id
            , List<String> children) {
        EntityDtoServiceRelation relationInfo = this.getRelation(null, dtoName);
        return this.getDtoById(relationInfo, id, children);
    }

    public <T> T getDtoById(Class dtoClass
            , Serializable id) {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoClass, null);
        return this.getDtoById(relationInfo, id, null);
    }

    public <T> T getDtoById(Class dtoClass
            , Serializable id
            , List<String> children) {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoClass, null);
        return this.getDtoById(relationInfo, id, children);
    }

    private <T> T getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , List<String> children)  {
        return this.selectService.getDtoById(relationInfo, id, children == null, children);
    }

    public <T> T getDtoByQueryWrapper(Object dto
            , QueryWrapper queryWrapper) {
        EntityDtoServiceRelation relationInfo = this.getRelation(dto.getClass(), null);
        return this.getDtoByQueryWrapper(relationInfo, dto, queryWrapper, null);
    }

    public <T> T getDtoByQueryWrapper(Object dto
            , QueryWrapper queryWrapper
            , List<String> children) {
        EntityDtoServiceRelation relationInfo = this.getRelation(dto.getClass(), null);
        return this.getDtoByQueryWrapper(relationInfo, dto, queryWrapper, children);
    }

    private <T> T getDtoByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto
            , QueryWrapper queryWrapper
            , List<String> children)  {
        return this.selectService.getDtoByQueryWrapper(relationInfo, dto, queryWrapper, children == null, children);
    }

    public <T> T getDtoByAnnotation(Object object) {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getDtoByAnnotation(relationInfo, object, null);
    }

    public <T> T getDtoByAnnotation(Object object
            , List<String> children) {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getDtoByAnnotation(relationInfo, object, children);
    }

    private <T> T getDtoByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , List<String> children)  {
        return this.selectService.getDtoByAnnotation(relationInfo, object, children == null, children);
    }

    public <T> PageInfo<T> getEntityPageByAnnotation(Object object
            , Integer pageIndex
            , Integer pageSize
    ) {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getEntityPageByAnnotation(relationInfo, object, pageIndex, pageSize);
    }

    private <T> PageInfo<T> getEntityPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer pageIndex
            , Integer pageSize
    )  {
        return this.selectService.getEntityPageByAnnotation(relationInfo
                , object
                , pageIndex, pageSize);
    }

    public <T> PageInfo<T> getEntityPageByQueryWrapper(Object dto
            , QueryWrapper queryWrapper
            , Integer pageIndex
            , Integer pageSize
    ) {
        EntityDtoServiceRelation relationInfo = this.getRelation(dto.getClass(), null);
        return this.getEntityPageByQueryWrapper(relationInfo,dto,queryWrapper,pageIndex,pageSize);
    }

    private <T> PageInfo<T> getEntityPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object object
            , QueryWrapper queryWrapper
            , Integer pageIndex
            , Integer pageSize
    )  {
        return this.selectService.getEntityPageByQueryWrapper(relationInfo,object,queryWrapper,pageIndex,pageSize);
    }

    public <T> PageInfo<T> getDtoPageByAnnotation(Object object
            , Integer pageIndex
            , Integer pageSize
    ) {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getDtoPageByAnnotation(relationInfo, object, null, pageIndex, pageSize);
    }

    public <T> PageInfo<T> getDtoPageByAnnotation(Object object, List<String> joinNames
            , Integer pageIndex
            , Integer pageSize
    ) {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getDtoPageByAnnotation(relationInfo, object, joinNames, pageIndex, pageSize);
    }

    private <T> PageInfo<T> getDtoPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, List<String> joinNames
            , Integer pageIndex
            , Integer pageSize
    )  {
        return this.selectService.getDtoPageByAnnotation(relationInfo
                , object, joinNames
                , pageIndex, pageSize);
    }

    public <T> PageInfo<T> getDtoPageByQueryWrapper(Object object
            , QueryWrapper queryWrapper
            , Integer pageIndex
            , Integer pageSize
    ) {
        return this.getDtoPageByQueryWrapper(object, queryWrapper, null, pageIndex, pageSize);
    }

    public <T> PageInfo<T> getDtoPageByQueryWrapper(Object object
            , QueryWrapper queryWrapper
            , List<String> joinNames
            , Integer pageIndex
            , Integer pageSize
    ) {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getDtoPageByQueryWrapper(relationInfo,object,queryWrapper, joinNames,pageIndex,pageSize);
    }

    private <T> PageInfo<T> getDtoPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object object
            , QueryWrapper queryWrapper
            , List<String> joinNames
            , Integer pageIndex
            , Integer pageSize
    )  {
        return this.selectService.getDtoPageByQueryWrapper(relationInfo,object, joinNames,queryWrapper,pageIndex,pageSize);
    }

    public List getEntityListByAnnotation(Object object
    ) {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getEntityListByAnnotation(relationInfo, object);
    }

    private List getEntityListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    )  {
        return this.selectService.getEntityListByAnnotation(relationInfo
                , object);
    }

    public <T> List<T> getEntityListByQueryWrapper(Object object
            , QueryWrapper queryWrapper
    ) {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getEntityListByQueryWrapper(relationInfo, object, queryWrapper);
    }

    private <T> List<T> getEntityListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object object
            , QueryWrapper queryWrapper
    )  {
        return this.selectService.getEntityListByQueryWrapper(relationInfo, object, queryWrapper);
    }

    public List getDtoListByAnnotation(Object object
    ) {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getDtoListByAnnotation(relationInfo, object, null);
    }

    private List getDtoListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, List<String> joinNames
    )  {
        return this.selectService.getDtoListByAnnotation(relationInfo, object, joinNames);
    }

    public List getDtoListByAnnotation(Object object, List<String> joinNames
    ) {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getDtoListByAnnotation(relationInfo, object, joinNames);
    }

    public <T> List<T> getDtoListByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper
    ) {
        return this.getDtoListByQueryWrapper(dtoClass, queryWrapper, null);
    }

    public <T> List<T> getDtoListByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper, List<String> joinNames
    ) {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoClass, null);
        return this.getDtoListByQueryWrapper(relationInfo, null, queryWrapper, joinNames);
    }


    private <T> List<T> getDtoListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object object
            , QueryWrapper queryWrapper, List<String> joinNames
    )  {
        return this.selectService.getDtoListByQueryWrapper(relationInfo, object, joinNames, queryWrapper);
    }

    protected <T> List<T> updateWithOptions(
            Object object, EntityDtoServiceRelation relationInfo
            , IUpdateProcessorProvider updateEntityProvider
            , IProviderParam options
            , String updateEventLogId) throws MybatisAccessorException {

        return updateService.updateByProviders(relationInfo
                , object, updateEntityProvider, options, updateEventLogId);
    }

    public <T> T save(Object object) throws MybatisAccessorException {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.save(relationInfo, object, null, false, null);
    }

    public <T> T save(Object object
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.save(relationInfo, object, children, updateChildrenOnly, updateEventLogId);
    }

    private <T> T save(EntityDtoServiceRelation relation
            , Object object
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        if(object == null) {
            return null;
        }
        IProviderParam providerParam = new DefaultEntityProviderParam(updateChildrenOnly, children);
        List<T> objects = updateWithOptions(object, relation, defaultInsertProcessorProvider
                , providerParam, updateEventLogId);
        return objects.get(0);
    }


    public <T> List<T> saveList(List objects) throws MybatisAccessorException {
        if(objects.isEmpty()) {
            return null;
        }
        EntityDtoServiceRelation relationInfo = this.getRelation(objects.get(0).getClass(), null);
        return this.saveList(relationInfo, objects, null, false, null);
    }

    public <T> List<T> saveList(List objects
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        if(objects.isEmpty()) {
            return null;
        }
        EntityDtoServiceRelation relationInfo = this.getRelation(objects.get(0).getClass(), null);
        return this.saveList(relationInfo, objects, children, updateChildrenOnly, updateEventLogId);
    }


    private <T> List<T> saveList(EntityDtoServiceRelation relation
            , List objectList
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        if(objectList == null || objectList.isEmpty()) {
            return null;
        }
        IProviderParam providerParam = new DefaultEntityProviderParam(updateChildrenOnly, children);
        return updateWithOptions(objectList, relation, defaultInsertProcessorProvider
                , providerParam, updateEventLogId);
    }

    public <T> T update(Object object) throws MybatisAccessorException {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.update(relationInfo, object
                ,  null, false, null);
    }

    public <T> T update(Object object
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.update(relationInfo, object
                ,  children, updateChildrenOnly, updateEventLogId);
    }

    private <T> T update(EntityDtoServiceRelation relation
            , Object object
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        IProviderParam providerParam = new DefaultEntityProviderParam(updateChildrenOnly, children);
        List<T> result = updateWithOptions(object, relation, defaultUpdateProcessorProvider
                , providerParam, updateEventLogId);
        return result.get(0);
    }

    public <T> List<T> updateList(List objects) throws MybatisAccessorException {
        if(objects == null || objects.isEmpty()) {
            return null;
        }
        EntityDtoServiceRelation relationInfo = this.getRelation(objects.get(0).getClass(), null);
        return this.updateList(relationInfo, objects, null
                , false, null);
    }

    public <T> List<T> updateList(List objects
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        if(objects == null || objects.isEmpty()) {
            return null;
        }
        EntityDtoServiceRelation relationInfo = this.getRelation(objects.get(0).getClass(), null);
        return this.updateList(relationInfo, objects, children
                , updateChildrenOnly, updateEventLogId);
    }


    public <T> List<T> updateList(EntityDtoServiceRelation relation, List objectList
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        if(objectList == null || objectList.isEmpty()) {
            return null;
        }

        IProviderParam providerParam = new DefaultEntityProviderParam(updateChildrenOnly, children);
        return updateWithOptions(objectList, relation, defaultUpdateProcessorProvider
                , providerParam, updateEventLogId);
    }

    public <T> T saveOrUpdate(Object object) throws MybatisAccessorException {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.saveOrUpdate(relationInfo, object
                ,  null, false, null);
    }

    public <T> T saveOrUpdate(Object object
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.saveOrUpdate(relationInfo, object
                ,  children, updateChildrenOnly, updateEventLogId);
    }


    private <T> T saveOrUpdate(EntityDtoServiceRelation relation
            , Object object
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        IProviderParam providerParam = new DefaultEntityProviderParam(updateChildrenOnly, children);
        List<T> result = updateWithOptions(object, relation, defaultSaveOrUpdateProcessorProvider
                , providerParam, updateEventLogId);
        return result.get(0);
    }

    public <T> List<T> saveOrUpdateList(List objects) throws MybatisAccessorException {
        if(objects == null || objects.isEmpty()) {
            return null;
        }
        EntityDtoServiceRelation relationInfo = this.getRelation(objects.get(0).getClass(), null);
        return this.saveOrUpdateList(relationInfo, objects, null
                , false, null);
    }

    public <T> List<T> saveOrUpdateList(List objects
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        if(objects == null || objects.isEmpty()) {
            return null;
        }
        EntityDtoServiceRelation relationInfo = this.getRelation(objects.get(0).getClass(), null);
        return this.saveOrUpdateList(relationInfo, objects, children
                , updateChildrenOnly, updateEventLogId);
    }


    public <T> List<T> saveOrUpdateList(EntityDtoServiceRelation relation, List objectList
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        if(objectList == null || objectList.isEmpty()) {
            return null;
        }

        IProviderParam providerParam = new DefaultEntityProviderParam(updateChildrenOnly, children);
        return updateWithOptions(objectList, relation, defaultSaveOrUpdateProcessorProvider
                , providerParam, updateEventLogId);
    }

    public <T> List<T> deleteByIds(Class dtoClass
            , Set<Serializable> ids) throws MybatisAccessorException {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoClass, null);
        return deleteByIds(relationInfo, ids, null, false, null);
    }

    public <T> List<T> deleteByIds(Class dtoClass
            , Set<Serializable> ids
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoClass, null);
        return deleteByIds(relationInfo, ids, children, updateChildrenOnly, updateEventLogId);
    }

    public <T> List<T> deleteByIds(String dtoName
            , Set<Serializable> ids
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        EntityDtoServiceRelation relationInfo = this.getRelation(null, dtoName);
        return deleteByIds(relationInfo, ids, children, updateChildrenOnly, updateEventLogId);
    }

    private <T> List<T> deleteByIds(EntityDtoServiceRelation relationInfo
            , Set<Serializable> ids
            , List<String> children
            , boolean updateChildrenOnly
            , String updateEventLogId) throws MybatisAccessorException {
        IProviderParam providerParam = new DefaultEntityProviderParam(updateChildrenOnly, children==null, children);
        return updateWithOptions(ids, relationInfo, defaultDeleteByIdProcessorProvider
                , providerParam, updateEventLogId);
    }

    public Long getCountByAnnotation(Object object) {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getCountByAnnotation(relationInfo, object);
    }

    public Long getCountByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    )  {
        return this.selectService.getCountByAnnotation(relationInfo
                , object);
    }

    public Long getCountByQueryWrapper(Object object
            , QueryWrapper queryWrapper) {
        EntityDtoServiceRelation relationInfo = this.getRelation(object.getClass(), null);
        return this.getCountByQueryWrapper(relationInfo, object, queryWrapper);
    }

    public Long getCountByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object object
            , QueryWrapper queryWrapper
    )  {
        return this.selectService.getCountByQueryWrapper(relationInfo, object, queryWrapper);
    }
}
