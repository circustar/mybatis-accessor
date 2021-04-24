package com.circustar.mybatis_accessor.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mybatis_accessor.provider.DefaultDeleteEntityProvider;
import com.circustar.mybatis_accessor.provider.DefaultInsertEntityProvider;
import com.circustar.mybatis_accessor.provider.DefaultUpdateEntityProvider;
import com.circustar.mybatis_accessor.relation.ScanRelationOnStartup;
import com.circustar.common_utils.collection.ArrayParamUtils;
import com.circustar.mybatis_accessor.common.MvcEnhanceConstants;
import com.circustar.mybatis_accessor.error.ResourceNotFoundException;
import com.circustar.mybatis_accessor.error.ValidateException;
import com.circustar.mybatis_accessor.provider.IUpdateEntityProvider;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.mybatis_accessor.response.PageInfo;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.service.IUpdateService;
import com.circustar.common_utils.reflection.ClassUtils;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.validator.DtoValidatorManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceSupport {
    protected ApplicationContext applicationContext;
    protected IUpdateService updateService = null;
    protected ISelectService selectService = null;
    protected DtoValidatorManager dtoValidatorManager = null;
    protected IEntityDtoServiceRelationMap entityDtoServiceRelationMap = null;
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected Map<String, EntityDtoServiceRelation> dtoNameMap = new ConcurrentHashMap<>();
    protected Map<String, IUpdateEntityProvider> providerMap = new ConcurrentHashMap<>();

    public ServiceSupport(ApplicationContext applicationContext, IEntityDtoServiceRelationMap entityDtoServiceRelationMap
            , ISelectService selectService, IUpdateService updateService, DtoValidatorManager dtoValidatorManager) {
        this.applicationContext = applicationContext;
        this.updateService = updateService;
        this.selectService = selectService;
        this.dtoValidatorManager = dtoValidatorManager;
        this.entityDtoServiceRelationMap = entityDtoServiceRelationMap;
    }

    protected Collection convertFromList(List mapList, Class clazz) {
        Class actualClass = mapList.get(0).getClass();
        if(!Map.class.isAssignableFrom(actualClass)) {
            return mapList;
        }
        List<Object> result = new ArrayList<>();
        for(Object map : mapList) {
            result.add(objectMapper.convertValue(map, clazz));
        }
        return result;

    }

    public Object convertFromMap(Object object, Class clazz) {
        if(object == null) {
            return null;
        } else if(object instanceof Map){
            return objectMapper.convertValue(object, clazz);
        } else if(object instanceof List){
            return this.convertFromList((List) object, clazz);
        } else if(object instanceof Collection){
            return this.convertFromList(new ArrayList((Collection) object), clazz);
        }
        return object;
    }

    public IUpdateEntityProvider parseProviderByName(String updateProviderName) throws ResourceNotFoundException {
        IUpdateEntityProvider provider = null;
        if(providerMap.containsKey(updateProviderName)) {
            provider = providerMap.get(updateProviderName);
        } else {
            if (applicationContext.containsBean(updateProviderName)) {
                provider = (IUpdateEntityProvider) applicationContext.getBean(updateProviderName);
            }
            providerMap.put(updateProviderName, provider);
        }
        if(provider == null) {
            throw new ResourceNotFoundException(updateProviderName);
        }
        return provider;
    }

    public EntityDtoServiceRelation parseEntityDtoServiceRelation(String dtoName) throws ResourceNotFoundException {
        EntityDtoServiceRelation relationInfo = null;
        if(dtoNameMap.containsKey(dtoName)) {
            relationInfo = dtoNameMap.get(dtoName);
        } else {
            String dtoClassName = FieldUtils.parseClassName(dtoName);
            relationInfo = this.entityDtoServiceRelationMap.getByDtoName(dtoClassName);
            dtoNameMap.put(dtoName, relationInfo);
        }
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dtoName);
        }
        return relationInfo;
    }

    public <T> T getEntityById(Class DtoClass
            , Serializable id) throws Exception {
        return this.getEntityById(DtoClass.getSimpleName(), id);
    }

    public <T> T getEntityById(String dtoName
            , Serializable id) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getEntityById(relationInfo, id);
    }

    public <T> T getEntityById(EntityDtoServiceRelation relationInfo
            , Serializable id) throws Exception {
        return this.selectService.getEntityById(relationInfo, id);
    }

    public <T> T getEntityByQueryWrapper(Class DtoClass
            , QueryWrapper queryWrapper) throws Exception {
        return this.getEntityByQueryWrapper(DtoClass.getSimpleName(), queryWrapper);
    }

    public <T> T getEntityByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getEntityByQueryWrapper(relationInfo, queryWrapper);
    }

    public <T> T getEntityByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper) throws Exception {
        return this.selectService.getEntityByQueryWrapper(relationInfo, queryWrapper);
    }

    public <T> T getEntityByAnnotation(Class DtoClass
            , Object object) throws Exception {
        return this.getEntityByAnnotation(DtoClass.getSimpleName(), object);
    }

    public <T> T getEntityByAnnotation(String dtoName
            , Object object) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getEntityByAnnotation(relationInfo, object);
    }

    public <T> T getEntityByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object) throws Exception {
        return this.selectService.getEntityByAnnotation(relationInfo, object);
    }

    public <T> T getDtoById(String dtoName
            , Serializable id
            , String children) throws Exception {
        return this.getDtoById(dtoName, id, ArrayParamUtils.convertStringToArray(children, ArrayParamUtils.DELIMITER_COMMA));
    }

    public <T> T getDtoById(Class dtoClass
            , Serializable id
            , String children) throws Exception {
        return this.getDtoById(dtoClass.getSimpleName(), id, children);
    }

    public <T> T getDtoById(String dtoName
            , Serializable id
            , String[] children) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getDtoById(relationInfo, id, children);
    }

    public <T> T getDtoById(Class dtoClass
            , Serializable id
            , String[] children) throws Exception {
        return this.getDtoById(dtoClass.getSimpleName(), id, children);
    }

    public <T> T getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children) throws Exception {
        return this.selectService.getDtoById(relationInfo, id, children);
    }

    public <T> T getDtoByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper
            , String children) throws Exception {
        return this.getDtoByQueryWrapper(dtoName, queryWrapper, ArrayParamUtils.convertStringToArray(children, ArrayParamUtils.DELIMITER_COMMA));
    }

    public <T> T getDtoByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper
            , String children) throws Exception {
        return this.getDtoByQueryWrapper(dtoClass.getSimpleName(), queryWrapper, children);
    }

    public <T> T getDtoByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper
            , String[] children) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getDtoByQueryWrapper(relationInfo, queryWrapper, children);
    }

    public <T> T getDtoByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper
            , String[] children) throws Exception {
        return this.getDtoByQueryWrapper(dtoClass.getSimpleName(), queryWrapper, children);
    }

    public <T> T getDtoByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
            , String[] children) throws Exception {
        return this.selectService.getDtoByQueryWrapper(relationInfo, queryWrapper, children);
    }

    public <T> T getDtoByAnnotation(String dtoName
            , Object object
            , String children) throws Exception {
        return this.getDtoByAnnotation(dtoName, object, ArrayParamUtils.convertStringToArray(children, ArrayParamUtils.DELIMITER_COMMA));
    }

    public <T> T getDtoByAnnotation(Class dtoClass
            , Object object
            , String children) throws Exception {
        return this.getDtoByAnnotation(dtoClass.getSimpleName(), object, children);
    }

    public <T> T getDtoByAnnotation(String dtoName
            , Object object
            , String[] children) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getDtoByAnnotation(relationInfo, object, children);
    }

    public <T> T getDtoByAnnotation(Class dtoClass
            , Object object
            , String[] children) throws Exception {
        return this.getDtoByAnnotation(dtoClass.getSimpleName(), object, children);
    }

    public <T> T getDtoByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , String[] children) throws Exception {
        return this.selectService.getDtoByAnnotation(relationInfo, object, children);
    }

    public <T> PageInfo<T> getEntityPageByAnnotation(Class dtoClass
            , Object object
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        return this.getEntityPageByAnnotation(dtoClass.getSimpleName(), object, page_index, page_size);
    }

    public <T> PageInfo<T> getEntityPageByAnnotation(String dtoName
            , Object object
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getEntityPageByAnnotation(relationInfo, object, page_index, page_size);
    }

    public <T> PageInfo<T> getEntityPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        return this.selectService.getEntityPageByAnnotation(relationInfo
                , convertFromMap(object, relationInfo.getDtoClass())
                , page_index, page_size);
    }

    public <T> PageInfo<T> getEntityPageByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        return this.getEntityPageByQueryWrapper(dtoClass.getSimpleName(),queryWrapper,page_index,page_size);
    }

    public <T> PageInfo<T> getEntityPageByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getEntityPageByQueryWrapper(relationInfo,queryWrapper,page_index,page_size);
    }

    public <T> PageInfo<T> getEntityPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        return this.selectService.getEntityPageByQueryWrapper(relationInfo,queryWrapper,null,page_index,page_size);
    }

    public <T> PageInfo<T> getDtoPageByAnnotation(Class dtoClass
            , Object object
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        return this.getDtoPageByAnnotation(dtoClass.getSimpleName(), object, page_index, page_size);
    }

    public <T> PageInfo<T> getDtoPageByAnnotation(String dtoName
            , Object object
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getDtoPageByAnnotation(relationInfo, object, page_index, page_size);
    }

    public <T> PageInfo<T> getDtoPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        return this.selectService.getDtoPageByAnnotation(relationInfo
                , convertFromMap(object, relationInfo.getDtoClass())
                , page_index, page_size);
    }

    public <T> PageInfo<T> getDtoPageByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        return this.getDtoPageByQueryWrapper(dtoClass.getSimpleName(),queryWrapper,page_index,page_size);
    }

    public <T> PageInfo<T> getDtoPageByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getDtoPageByQueryWrapper(relationInfo,queryWrapper,page_index,page_size);
    }

    public <T> PageInfo<T> getDtoPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        return this.selectService.getDtoPageByQueryWrapper(relationInfo,queryWrapper,page_index,page_size);
    }

    public List getEntityListByAnnotation(Class dtoClass
            , Object object
    ) throws Exception {
        return this.getEntityListByAnnotation(dtoClass.getSimpleName(), object);
    }

    public List getEntityListByAnnotation(String dtoName
            , Object object
    ) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getEntityListByAnnotation(relationInfo, this.convertFromMap(object, relationInfo.getDtoClass()));
    }

    public List getEntityListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) throws Exception {
        return this.selectService.getEntityListByAnnotation(relationInfo
                , this.convertFromMap(object, relationInfo.getDtoClass()));
    }

    public <T> List<T> getEntityListByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper
    ) throws Exception {
        return this.getEntityListByQueryWrapper(dtoClass.getSimpleName(), queryWrapper);
    }

    public <T> List<T> getEntityListByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper
    ) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getEntityListByQueryWrapper(relationInfo, queryWrapper);
    }

    public <T> List<T> getEntityListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
    ) throws Exception {
        return this.selectService.getEntityListByQueryWrapper(relationInfo, queryWrapper, null);
    }

    public List getDtoListByAnnotation(Class dtoClass
            , Object object
    ) throws Exception {
        return this.getDtoListByAnnotation(dtoClass.getSimpleName(), object);
    }

    public List getDtoListByAnnotation(String dtoName
            , Object object
    ) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getDtoListByAnnotation(relationInfo, this.convertFromMap(object, relationInfo.getDtoClass()));
    }

    public List getDtoListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) throws Exception {
        return this.selectService.getDtoListByAnnotation(relationInfo
                , this.convertFromMap(object, relationInfo.getDtoClass()));
    }

    public <T> List<T> getDtoListByQueryWrapper(Class dtoClass
            , QueryWrapper queryWrapper
    ) throws Exception {
        return this.getDtoListByQueryWrapper(dtoClass.getSimpleName(), queryWrapper);
    }

    public <T> List<T> getDtoListByQueryWrapper(String dtoName
            , QueryWrapper queryWrapper
    ) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getDtoListByQueryWrapper(relationInfo, queryWrapper);
    }

    public <T> List<T> getDtoListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
    ) throws Exception {
        return this.selectService.getDtoListByQueryWrapper(relationInfo, queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> List<T> updateWithOptions(
            String dtoName, Object dtoObject
            , String providerName, Map options) throws Exception {
        IUpdateEntityProvider updateEntityProvider = this.parseProviderByName(providerName);
        return updateWithOptions(dtoName, dtoObject, updateEntityProvider, options);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> List<T> updateWithOptions(
            String dtoName, Object objectOrMap
            , IUpdateEntityProvider updateEntityProvider, Map options) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return updateWithOptions(objectOrMap, relationInfo, updateEntityProvider, options);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> List<T> updateWithOptions(
            Object objectOrMap, EntityDtoServiceRelation relationInfo
            , IUpdateEntityProvider updateEntityProvider
            , Map options) throws Exception {

        Object dtoObject = convertFromMap(objectOrMap, relationInfo.getDtoClass());
        if(ScanRelationOnStartup.enableSpringValidation) {
            BindException errors  = new BindException(dtoObject, relationInfo.getDtoClass().getSimpleName());
            this.dtoValidatorManager.validate(dtoObject, updateEntityProvider, errors);
            if(errors.hasErrors()) {
                throw new ValidateException("validate failed", errors);
            }
        }
        List<T> updatedEntities = updateService.updateByProviders(relationInfo
                , dtoObject, updateEntityProvider, options);

        return updatedEntities;
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> T save(Object object
            , String children
            , boolean updateChildrenOnly) throws Exception {
        return this.save(object.getClass().getSimpleName(), object, children, updateChildrenOnly);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> T save(String dtoName
            , Object objectOrMap
            , String children
            , boolean updateChildrenOnly) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return save(objectOrMap,  relationInfo, children, updateChildrenOnly);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> T save(Object objectOrMap
            , EntityDtoServiceRelation relation
            , String children
            , boolean updateChildrenOnly) throws Exception {
        if(objectOrMap == null) {
            return null;
        }
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        List<T> objects = updateWithOptions(objectOrMap, relation, DefaultInsertEntityProvider.getInstance()
                , options);
        return objects.get(0);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> List<T> saveList(List objects
            , String children
            , boolean updateChildrenOnly) throws Exception {
        if(objects.size() <= 0) {
            return null;
        }
        String dtoName = objects.get(0).getClass().getSimpleName();
        return this.saveList(dtoName, objects, children, updateChildrenOnly);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> List<T> saveList(String dtoName
            , List objectListOrMapList
            , String children
            , boolean updateChildrenOnly) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.saveList(objectListOrMapList, relationInfo, children, updateChildrenOnly);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> List<T> saveList(List objectListOrMapList
            , EntityDtoServiceRelation relation
            , String children
            , boolean updateChildrenOnly) throws Exception {
        if(objectListOrMapList == null || objectListOrMapList.size() <= 0) {
            return null;
        }
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);

        return updateWithOptions(objectListOrMapList, relation, DefaultInsertEntityProvider.getInstance()
                , options);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> T update(Object object
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete) throws Exception {
        return this.update(object.getClass().getSimpleName(), object
                , children, updateChildrenOnly, removeAndInsertNewChild, physicDelete);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> T update(String dtoName
            , Object objectOrMap
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return update(objectOrMap, relationInfo
                , children, updateChildrenOnly, removeAndInsertNewChild, physicDelete);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> T update(Object objectOrMap
            , EntityDtoServiceRelation relation
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_AND_INSERT, removeAndInsertNewChild);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);

        List<T> result = updateWithOptions(objectOrMap, relation, DefaultUpdateEntityProvider.getInstance()
                , options);
        return result.get(0);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> List<T> updateList(List objects
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete) throws Exception {
        String dtoName = ((Class)ClassUtils.getFirstTypeArgument(objects.getClass())).getSimpleName();
        return this.updateList(dtoName, objects, children
                , updateChildrenOnly, removeAndInsertNewChild, physicDelete);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> List<T> updateList(String dtoName
            , List objectListOrMapList
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return updateList(objectListOrMapList, relationInfo, children
                , updateChildrenOnly, removeAndInsertNewChild, physicDelete);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> List<T> updateList(List objectListOrMapList
            , EntityDtoServiceRelation relation
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_AND_INSERT, removeAndInsertNewChild);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);

        return updateWithOptions(objectListOrMapList, relation, DefaultUpdateEntityProvider.getInstance()
                , options);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> T deleteById(String dtoName, Serializable id, String children
            , boolean updateChildrenOnly
            , boolean physicDelete) throws Exception  {
        List<T> result = deleteByIds(dtoName, Collections.singleton(id), ArrayParamUtils.convertStringToArray(children)
                , updateChildrenOnly, physicDelete);
        return result.iterator().next();
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> List<T> deleteByIds(String dtoName
            , Set<Serializable> ids
            , String[] children
            , boolean updateChildrenOnly
            , boolean physicDelete) throws Exception  {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return deleteByIds(ids, relationInfo, children, updateChildrenOnly
                , physicDelete);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> List<T> deleteByIds(Set<Serializable> ids
            , EntityDtoServiceRelation relationInfo
            , String[] children
            , boolean updateChildrenOnly
            , boolean physicDelete) throws Exception  {

        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, children);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);

        return updateWithOptions(ids, relationInfo, DefaultDeleteEntityProvider.getInstance()
                , options);
    }

}
