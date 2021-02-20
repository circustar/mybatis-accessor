package com.circustar.mvcenhance.support;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    protected IUpdateEntityProvider parseProviderByName(String updateProviderName) throws ResourceNotFoundException {
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

    public Collection<Object> updateObject(
            String dtoName, Object dtoObject, EntityDtoServiceRelation relationInfo
            , IUpdateEntityProvider updateEntityProvider, Map options) throws Exception {

        BindException errors  = new BindException(dtoObject, dtoName);
        this.dtoValidatorManager.validate(dtoObject, updateEntityProvider, errors);
        if(errors.hasErrors()) {
            throw new ValidateException("validate failed", errors);
        }
        Collection<Object> updatedEntities = updateService.updateByProviders(relationInfo
                , dtoObject, updateEntityProvider, options);

        return updatedEntities;
    }

    public Object getById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children) throws Exception {
        return this.selectService.getById(relationInfo, id, children);
    }

    public <T> PageInfo<T> getPagesByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        return this.selectService.getPagesByAnnotation(relationInfo, object, page_index, page_size);
    }

    public <T> PageInfo<T> getPagesByWrapper(EntityDtoServiceRelation relationInfo
            , List<WrapperPiece> queryFiledModelList
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        return this.selectService.getPagesByWrapper(relationInfo,queryFiledModelList,page_index,page_size);
    }

    public List getListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) throws Exception {
        return this.selectService.getListByAnnotation(relationInfo, object);
    }

    public <T> List<T> getListByWrapper(EntityDtoServiceRelation relationInfo
            , List<WrapperPiece> queryFiledModelList
    ) throws Exception {
        return this.selectService.getListByWrapper(relationInfo, queryFiledModelList);
    }
}
