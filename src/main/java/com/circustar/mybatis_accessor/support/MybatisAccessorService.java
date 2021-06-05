package com.circustar.mybatis_accessor.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mybatis_accessor.common.MessageProperties;
import com.circustar.mybatis_accessor.provider.DefaultDeleteEntityProvider;
import com.circustar.mybatis_accessor.provider.DefaultInsertEntityProvider;
import com.circustar.mybatis_accessor.provider.DefaultUpdateEntityProvider;
import com.circustar.common_utils.collection.ArrayParamUtils;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.provider.IUpdateEntityProvider;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.mybatis_accessor.response.PageInfo;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.service.IUpdateService;
import com.circustar.common_utils.reflection.FieldUtils;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MybatisAccessorService {
    protected ApplicationContext applicationContext;
    protected IUpdateService updateService = null;
    protected ISelectService selectService = null;
    protected IEntityDtoServiceRelationMap entityDtoServiceRelationMap = null;
    protected Map<String, EntityDtoServiceRelation> dtoNameMap = new ConcurrentHashMap<>();

    public MybatisAccessorService(ApplicationContext applicationContext, IEntityDtoServiceRelationMap entityDtoServiceRelationMap
            , ISelectService selectService, IUpdateService updateService) {
        this.applicationContext = applicationContext;
        this.updateService = updateService;
        this.selectService = selectService;
        this.entityDtoServiceRelationMap = entityDtoServiceRelationMap;
    }

    private EntityDtoServiceRelation getEntityDtoServiceRelation(String dtoName) {
        String dtoClassName = FieldUtils.parseClassName(dtoName);
        EntityDtoServiceRelation relationInfo = this.entityDtoServiceRelationMap.getByDtoName(dtoClassName);
        return relationInfo;
    }

    public EntityDtoServiceRelation getRelation(String dtoName) {
        EntityDtoServiceRelation relationInfo = getRelationIfExist(dtoName);
        if (relationInfo == null) {
            throw new RuntimeException(String.format(MessageProperties.DTO_NAME_NOT_FOUND, dtoName));
        }
        return relationInfo;
    }

    public EntityDtoServiceRelation getRelationIfExist(String dtoName) {
        EntityDtoServiceRelation relationInfo = null;
        if(dtoNameMap.containsKey(dtoName)) {
            relationInfo = dtoNameMap.getOrDefault(dtoName, null);
        } else {
            relationInfo = getEntityDtoServiceRelation(dtoName);
            dtoNameMap.put(dtoName, relationInfo);
        }
        return relationInfo;
    }

    public <T> T getEntityById(Class DtoClass
            , Serializable id)  {
        return this.getEntityById(DtoClass.getSimpleName(), id);
    }

    public <T> T getEntityById(String dtoName
            , Serializable id)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return this.getEntityById(relationInfo, id);
    }

    public <T> T getEntityById(EntityDtoServiceRelation relationInfo
            , Serializable id)  {
        return this.selectService.getEntityById(relationInfo, id);
    }

    public <T> T getEntityByQueryWrapper(Class DtoClass
            , QueryWrapper queryWrapper)  {
        return this.getEntityByQueryWrapper(DtoClass.getSimpleName(), queryWrapper);
    }

    public <T> T getEntityByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return this.getEntityByQueryWrapper(relationInfo, queryWrapper);
    }

    public <T> T getEntityByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper)  {
        return this.selectService.getEntityByQueryWrapper(relationInfo, queryWrapper);
    }

    public <T> T getEntityByAnnotation(Object object)  {
        return this.getEntityByAnnotation(object.getClass(), object);
    }

    public <T> T getEntityByAnnotation(Class DtoClass
            , Object object)  {
        return this.getEntityByAnnotation(DtoClass.getSimpleName(), object);
    }

    public <T> T getEntityByAnnotation(String dtoName
            , Object object)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return this.getEntityByAnnotation(relationInfo, object);
    }

    public <T> T getEntityByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object)  {
        return this.selectService.getEntityByAnnotation(relationInfo, object);
    }

    public <T> T getDtoById(String dtoName
            , Serializable id
            , String children)  {
        return this.getDtoById(dtoName, id, ArrayParamUtils.convertStringToArray(children, ArrayParamUtils.DELIMITER_COMMA));
    }

    public <T> T getDtoById(Class dtoClass
            , Serializable id
            , String children)  {
        return this.getDtoById(dtoClass.getSimpleName(), id, children);
    }

    public <T> T getDtoById(String dtoName
            , Serializable id
            , String[] children)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return this.getDtoById(relationInfo, id, children);
    }

    public <T> T getDtoById(Class dtoClass
            , Serializable id
            , String[] children)  {
        return this.getDtoById(dtoClass.getSimpleName(), id, children);
    }

    public <T> T getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children)  {
        return this.selectService.getDtoById(relationInfo, id, children);
    }

    public <T> T getDtoByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper
            , String children)  {
        return this.getDtoByQueryWrapper(dtoName, queryWrapper, ArrayParamUtils.convertStringToArray(children, ArrayParamUtils.DELIMITER_COMMA));
    }

    public <T> T getDtoByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper
            , String children)  {
        return this.getDtoByQueryWrapper(dtoClass.getSimpleName(), queryWrapper, children);
    }

    public <T> T getDtoByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper
            , String[] children)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return this.getDtoByQueryWrapper(relationInfo, queryWrapper, children);
    }

    public <T> T getDtoByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper
            , String[] children)  {
        return this.getDtoByQueryWrapper(dtoClass.getSimpleName(), queryWrapper, children);
    }

    public <T> T getDtoByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
            , String[] children)  {
        return this.selectService.getDtoByQueryWrapper(relationInfo, queryWrapper, children);
    }

    public <T> T getDtoByAnnotation(Object object
            , String children)  {
        return this.getDtoByAnnotation(object.getClass(), object, children);
    }

    public <T> T getDtoByAnnotation(String dtoName
            , Object object
            , String children)  {
        return this.getDtoByAnnotation(dtoName, object, ArrayParamUtils.convertStringToArray(children, ArrayParamUtils.DELIMITER_COMMA));
    }

    public <T> T getDtoByAnnotation(Class dtoClass
            , Object object
            , String children)  {
        return this.getDtoByAnnotation(dtoClass.getSimpleName(), object, children);
    }

    public <T> T getDtoByAnnotation(String dtoName
            , Object object
            , String[] children)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return this.getDtoByAnnotation(relationInfo, object, children);
    }

    public <T> T getDtoByAnnotation(Class dtoClass
            , Object object
            , String[] children)  {
        return this.getDtoByAnnotation(dtoClass.getSimpleName(), object, children);
    }

    public <T> T getDtoByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , String[] children)  {
        return this.selectService.getDtoByAnnotation(relationInfo, object, children);
    }

    public <T> PageInfo<T> getEntityPageByAnnotation(Object object
            , Integer page_index
            , Integer page_size
    )  {
        return this.getEntityPageByAnnotation(object.getClass(), object, page_index, page_size);
    }

    public <T> PageInfo<T> getEntityPageByAnnotation(Class dtoClass
            , Object object
            , Integer page_index
            , Integer page_size
    )  {
        return this.getEntityPageByAnnotation(dtoClass.getSimpleName(), object, page_index, page_size);
    }

    public <T> PageInfo<T> getEntityPageByAnnotation(String dtoName
            , Object object
            , Integer page_index
            , Integer page_size
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
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

    public <T> PageInfo<T> getEntityPageByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    )  {
        return this.getEntityPageByQueryWrapper(dtoClass.getSimpleName(),queryWrapper,page_index,page_size);
    }

    public <T> PageInfo<T> getEntityPageByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return this.getEntityPageByQueryWrapper(relationInfo,queryWrapper,page_index,page_size);
    }

    public <T> PageInfo<T> getEntityPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    )  {
        return this.selectService.getEntityPageByQueryWrapper(relationInfo,queryWrapper,null,page_index,page_size);
    }

    public <T> PageInfo<T> getDtoPageByAnnotation(Object object
            , Integer page_index
            , Integer page_size
    )  {
        return this.getDtoPageByAnnotation(object.getClass(), object, page_index, page_size);
    }

    public <T> PageInfo<T> getDtoPageByAnnotation(Class dtoClass
            , Object object
            , Integer page_index
            , Integer page_size
    )  {
        return this.getDtoPageByAnnotation(dtoClass.getSimpleName(), object, page_index, page_size);
    }

    public <T> PageInfo<T> getDtoPageByAnnotation(String dtoName
            , Object object
            , Integer page_index
            , Integer page_size
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
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

    public <T> PageInfo<T> getDtoPageByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    )  {
        return this.getDtoPageByQueryWrapper(dtoClass.getSimpleName(),queryWrapper,page_index,page_size);
    }

    public <T> PageInfo<T> getDtoPageByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return this.getDtoPageByQueryWrapper(relationInfo,queryWrapper,page_index,page_size);
    }

    public <T> PageInfo<T> getDtoPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    )  {
        return this.selectService.getDtoPageByQueryWrapper(relationInfo,queryWrapper,page_index,page_size);
    }

    public List getEntityListByAnnotation(Object object
    )  {
        return this.getEntityListByAnnotation(object.getClass(), object);
    }

    public List getEntityListByAnnotation(Class dtoClass
            , Object object
    )  {
        return this.getEntityListByAnnotation(dtoClass.getSimpleName(), object);
    }

    public List getEntityListByAnnotation(String dtoName
            , Object object
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return this.getEntityListByAnnotation(relationInfo, object);
    }

    public List getEntityListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    )  {
        return this.selectService.getEntityListByAnnotation(relationInfo
                , object);
    }

    public <T> List<T> getEntityListByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper
    )  {
        return this.getEntityListByQueryWrapper(dtoClass.getSimpleName(), queryWrapper);
    }

    public <T> List<T> getEntityListByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return this.getEntityListByQueryWrapper(relationInfo, queryWrapper);
    }

    public <T> List<T> getEntityListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
    )  {
        return this.selectService.getEntityListByQueryWrapper(relationInfo, queryWrapper, null);
    }

    public List getDtoListByAnnotation(Object object
    )  {
        return this.getDtoListByAnnotation(object.getClass(), object);
    }

    public List getDtoListByAnnotation(Class dtoClass
            , Object object
    )  {
        return this.getDtoListByAnnotation(dtoClass.getSimpleName(), object);
    }

    public List getDtoListByAnnotation(String dtoName
            , Object object
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
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
        return this.getDtoListByQueryWrapper(dtoClass.getSimpleName(), queryWrapper);
    }

    public <T> List<T> getDtoListByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper
    )  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return this.getDtoListByQueryWrapper(relationInfo, queryWrapper);
    }

    public <T> List<T> getDtoListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
    )  {
        return this.selectService.getDtoListByQueryWrapper(relationInfo, queryWrapper);
    }

    public <T> List<T> updateWithOptions(
            Object object, EntityDtoServiceRelation relationInfo
            , IUpdateEntityProvider updateEntityProvider
            , Map options)  {

        List<T> updatedEntities = updateService.updateByProviders(relationInfo
                , object, updateEntityProvider, options);

        return updatedEntities;
    }


    public <T> T save(Object object
            , boolean includeAllChildren
            , String children
            , boolean updateChildrenOnly)  {
        return this.save(object.getClass().getSimpleName(), object, includeAllChildren, children, updateChildrenOnly);
    }


    public <T> T save(String dtoName
            , Object object
            , boolean includeAllChildren
            , String children
            , boolean updateChildrenOnly)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return save(object,  relationInfo, includeAllChildren, children, updateChildrenOnly);
    }


    public <T> T save(Object object
            , EntityDtoServiceRelation relation
            , boolean includeAllChildren
            , String children
            , boolean updateChildrenOnly)  {
        if(object == null) {
            return null;
        }
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_INCLUDE_ALL_CHILDREN, includeAllChildren);
        List<T> objects = updateWithOptions(object, relation, DefaultInsertEntityProvider.getInstance()
                , options);
        return objects.get(0);
    }


    public <T> List<T> saveList(List objects
            , boolean includeAllChildren
            , String children
            , boolean updateChildrenOnly)  {
        if(objects.size() <= 0) {
            return null;
        }
        String dtoName = objects.get(0).getClass().getSimpleName();
        return this.saveList(dtoName, objects, includeAllChildren, children, updateChildrenOnly);
    }


    public <T> List<T> saveList(String dtoName
            , List objectList
            , boolean includeAllChildren
            , String children
            , boolean updateChildrenOnly)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return this.saveList(objectList, relationInfo, includeAllChildren, children, updateChildrenOnly);
    }


    public <T> List<T> saveList(List objectList
            , EntityDtoServiceRelation relation
            , boolean includeAllChildren
            , String children
            , boolean updateChildrenOnly)  {
        if(objectList == null || objectList.size() <= 0) {
            return null;
        }
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_INCLUDE_ALL_CHILDREN, includeAllChildren);

        return updateWithOptions(objectList, relation, DefaultInsertEntityProvider.getInstance()
                , options);
    }


    public <T> T update(Object object
            , boolean includeAllChildren
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild)  {
        return this.update(object.getClass().getSimpleName(), object
                , includeAllChildren,  children, updateChildrenOnly, removeAndInsertNewChild);
    }


    public <T> T update(String dtoName
            , Object object
            , boolean includeAllChildren
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return update(object, relationInfo
                , includeAllChildren,  children, updateChildrenOnly, removeAndInsertNewChild);
    }


    public <T> T update(Object object
            , EntityDtoServiceRelation relation
            , boolean includeAllChildren
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild)  {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_AND_INSERT, removeAndInsertNewChild);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_INCLUDE_ALL_CHILDREN, includeAllChildren);

        List<T> result = updateWithOptions(object, relation, DefaultUpdateEntityProvider.getInstance()
                , options);
        return result.get(0);
    }


    public <T> List<T> updateList(List objects
            , boolean includeAllChildren
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild)  {
        if(objects == null || objects.size() == 0) {
            return null;
        }
        String dtoName = objects.get(0).getClass().getSimpleName();
        return this.updateList(dtoName, objects, includeAllChildren, children
                , updateChildrenOnly, removeAndInsertNewChild);
    }


    public <T> List<T> updateList(String dtoName
            , List objectList
            , boolean includeAllChildren
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild)  {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return updateList(objectList, relationInfo, includeAllChildren, children
                , updateChildrenOnly, removeAndInsertNewChild);
    }


    public <T> List<T> updateList(List objectList
            , EntityDtoServiceRelation relation
            , boolean includeAllChildren
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild)  {
        if(objectList == null || objectList.size() == 0) {
            return null;
        }
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_AND_INSERT, removeAndInsertNewChild);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_INCLUDE_ALL_CHILDREN, includeAllChildren);

        return updateWithOptions(objectList, relation, DefaultUpdateEntityProvider.getInstance()
                , options);
    }


    public <T> T deleteById(String dtoName, Serializable id, String children
            , boolean updateChildrenOnly) {
        List<T> result = deleteByIds(dtoName, Collections.singleton(id), ArrayParamUtils.convertStringToArray(children)
                , updateChildrenOnly);
        return result.iterator().next();
    }

    public <T> List<T> deleteByIds(String dtoName
            , Set<Serializable> ids
            , String children
            , boolean updateChildrenOnly) {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return deleteByIds(ids, relationInfo, ArrayParamUtils.convertStringToArray(children), updateChildrenOnly);
    }

    public <T> List<T> deleteByIds(String dtoName
            , Set<Serializable> ids
            , String[] children
            , boolean updateChildrenOnly) {
        EntityDtoServiceRelation relationInfo = this.getRelation(dtoName);
        return deleteByIds(ids, relationInfo, children, updateChildrenOnly);
    }


    public <T> List<T> deleteByIds(Set<Serializable> ids
            , EntityDtoServiceRelation relationInfo
            , String[] children
            , boolean updateChildrenOnly) {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_LIST, children);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);

        return updateWithOptions(ids, relationInfo, DefaultDeleteEntityProvider.getInstance()
                , options);
    }

}
