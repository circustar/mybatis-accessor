package com.circustar.mvcenhance.support;

import com.circustar.mvcenhance.provider.DefaultDeleteEntityProvider;
import com.circustar.mvcenhance.provider.DefaultInsertEntityProvider;
import com.circustar.mvcenhance.provider.DefaultUpdateEntityProvider;
import com.circustar.mvcenhance.utils.ArrayParamUtils;
import com.circustar.mvcenhance.utils.MvcEnhanceConstants;
import com.circustar.mvcenhance.wrapper.SimpleWrapperPiece;
import com.circustar.mvcenhance.wrapper.WrapperPiece;
import com.circustar.mvcenhance.error.ResourceNotFoundException;
import com.circustar.mvcenhance.error.ValidateException;
import com.circustar.mvcenhance.provider.IUpdateEntityProvider;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.response.PageInfo;
import com.circustar.mvcenhance.service.ISelectService;
import com.circustar.mvcenhance.service.IUpdateService;
import com.circustar.mvcenhance.utils.ClassUtils;
import com.circustar.mvcenhance.utils.FieldUtils;
import com.circustar.mvcenhance.validator.DtoValidatorManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindException;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    public List<Object> convertFromMapList(List<Object> mapList, Class clazz) {
        Class actualClass = (Class) ClassUtils.getFirstTypeArgument(mapList.getClass());
        List<Object> objects = mapList;
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
        if(object instanceof Map){
            return objectMapper.convertValue(object, clazz);
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

    public Object getById(String dtoName
            , Serializable id
            , String children) throws Exception {
        return this.getById(dtoName, id, ArrayParamUtils.convertStringToArray(children, ArrayParamUtils.DELIMITER_COMMA));
    }

    public Object getById(String dtoName
            , Serializable id
            , String[] children) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getById(relationInfo, id, children);
    }

    public Object getById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children) throws Exception {
        return this.selectService.getById(relationInfo, id, children);
    }

    public <T> PageInfo<T> getPagesByAnnotation(String dtoName
            , Object object
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getPagesByAnnotation(relationInfo, object, page_index, page_size);
    }

    public <T> PageInfo<T> getPagesByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        return this.selectService.getPagesByAnnotation(relationInfo, object, page_index, page_size);
    }

    public <T> PageInfo<T> getPagesBySimpleWrapper(String dtoName
            , List<SimpleWrapperPiece> queryFiledModelList
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getPagesByWrapper(relationInfo
                ,queryFiledModelList.stream().map(x -> x.convertToWrapperPiece(relationInfo)).collect(Collectors.toList())
                ,page_index,page_size);
    }

    public <T> PageInfo<T> getPagesByWrapper(String dtoName
            , List<WrapperPiece> queryFiledModelList
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getPagesByWrapper(relationInfo,queryFiledModelList,page_index,page_size);
    }

    public <T> PageInfo<T> getPagesByWrapper(EntityDtoServiceRelation relationInfo
            , List<WrapperPiece> queryFiledModelList
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        return this.selectService.getPagesByWrapper(relationInfo,queryFiledModelList,page_index,page_size);
    }

    public List getListByAnnotation(String dtoName
            , Object object
    ) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getListByAnnotation(relationInfo, object);
    }

    public List getListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) throws Exception {
        return this.selectService.getListByAnnotation(relationInfo, object);
    }

    public <T> List<T> getListByWrapper(String dtoName
            , List<WrapperPiece> queryFiledModelList
    ) throws ResourceNotFoundException {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return this.getListByWrapper(relationInfo, queryFiledModelList);
    }

    public <T> List<T> getListByWrapper(EntityDtoServiceRelation relationInfo
            , List<WrapperPiece> queryFiledModelList
    ) {
        return this.selectService.getListByWrapper(relationInfo, queryFiledModelList);
    }

    public List<Object> updateWithOptions(
            String dtoName, Object dtoObject
            , String providerName, Map options) throws Exception {
        IUpdateEntityProvider updateEntityProvider = this.parseProviderByName(providerName);
        return updateWithOptions(dtoName, dtoObject, updateEntityProvider, options);
    }

    public List<Object> updateWithOptions(
            String dtoName, Object dtoObject
            , IUpdateEntityProvider updateEntityProvider, Map options) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return updateWithOptions(dtoName, dtoObject, relationInfo, updateEntityProvider, options);
    }

    public List<Object> updateWithOptions(
            String dtoName, Object dtoObject, EntityDtoServiceRelation relationInfo
            , IUpdateEntityProvider updateEntityProvider, Map options) throws Exception {

        BindException errors  = new BindException(dtoObject, dtoName);
        this.dtoValidatorManager.validate(dtoObject, updateEntityProvider, errors);
        if(errors.hasErrors()) {
            throw new ValidateException("validate failed", errors);
        }
        List<Object> updatedEntities = updateService.updateByProviders(relationInfo
                , dtoObject, updateEntityProvider, options);

        return updatedEntities;
    }


    public List<Object> save(String dtoName
            , Object updateObject
            , String children
            , boolean updateChildrenOnly) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        Object dto = this.convertFromMap(updateObject, relationInfo.getDtoClass());
        return save(dtoName, dto,  relationInfo, children, updateChildrenOnly);
    }

    public List<Object> save(String dtoName
            , Object updateObject
            , EntityDtoServiceRelation relation
            , String children
            , boolean updateChildrenOnly) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        return updateWithOptions(dtoName, updateObject, relation, DefaultInsertEntityProvider.getInstance()
                , options);
    }

    public List<Object> saveList(String dtoName
            , List<Object> mapList
            , String children
            , boolean updateChildrenOnly) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        List<Object> objects = this.convertFromMapList(mapList, relationInfo.getDtoClass());
        return saveList(dtoName, objects, relationInfo, children, updateChildrenOnly);
    }

    public List<Object> saveList(String dtoName
            , List<Object> objects
            , EntityDtoServiceRelation relation
            , String children
            , boolean updateChildrenOnly) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);

        return updateWithOptions(dtoName, objects, relation, DefaultInsertEntityProvider.getInstance()
                , options);
    }

    public List<Object> update(String dtoName
            , Object updateObject
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        Object dto = this.convertFromMap(updateObject, relationInfo.getDtoClass());

        return update(dtoName, dto, relationInfo
                , children, updateChildrenOnly, removeAndInsertNewChild, physicDelete);
    }

    public List<Object> update(String dtoName
            , Object updateObject
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

        return updateWithOptions(dtoName, updateObject, relation, DefaultUpdateEntityProvider.getInstance()
                , options);
    }

    public List<Object> updateList(String dtoName
            , List<Object> mapList
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        List<Object> objects = this.convertFromMapList(mapList, relationInfo.getDtoClass());
        return updateList(dtoName, objects, relationInfo, children
                , updateChildrenOnly, removeAndInsertNewChild, physicDelete);
    }

    public List<Object> updateList(String dtoName
            , List<Object> objects
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

        return updateWithOptions(dtoName, objects, relation, DefaultUpdateEntityProvider.getInstance()
                , options);
    }

    public Object deleteById(String dtoName, String children
            , Serializable id
            , boolean updateChildrenOnly
            , boolean physicDelete) throws Exception  {
        List<Object> result = deleteByIds(dtoName, ArrayParamUtils.convertStringToArray(children), Collections.singleton(id)
                , updateChildrenOnly, physicDelete);
        return result.iterator().next();
    }

    public List<Object> deleteByIds(String dtoName
            , String[] children
            , Set<Serializable> ids
            , boolean updateChildrenOnly
            , boolean physicDelete) throws Exception  {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);

        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, children);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);

        return updateWithOptions(dtoName, ids, relationInfo, DefaultDeleteEntityProvider.getInstance()
                , options);
    }

}
