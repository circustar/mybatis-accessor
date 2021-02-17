package com.circustar.mvcenhance.support;

import com.circustar.mvcenhance.error.ResourceNotFoundException;
import com.circustar.mvcenhance.annotation.QueryFieldModel;
import com.circustar.mvcenhance.error.ValidateException;
import com.circustar.mvcenhance.response.DefaultServiceResult;
import com.circustar.mvcenhance.response.IServiceResult;
import com.circustar.mvcenhance.response.PageInfo;
import com.circustar.mvcenhance.utils.ClassUtils;
import com.circustar.mvcenhance.utils.MvcEnhanceConstants;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.service.IUpdateService;
import com.circustar.mvcenhance.service.ISelectService;
import com.circustar.mvcenhance.provider.*;
import com.circustar.mvcenhance.utils.ArrayParamUtils;
import com.circustar.mvcenhance.utils.FieldUtils;
import com.circustar.mvcenhance.validator.DtoValidatorManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// TODO:bug fix
// 相同列名
// group


public class ControllerSupport implements ApplicationContextAware {
    protected IEntityDtoServiceRelationMap entityDtoServiceRelationMap = null;
    protected ApplicationContext applicationContext;
    protected IUpdateService updateService = null;
    protected ISelectService selectService = null;
    protected Map<String, IUpdateEntityProvider> providerMap = new ConcurrentHashMap<>();
    protected DtoValidatorManager dtoValidatorManager = null;
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected Map<String, EntityDtoServiceRelation> dtoNameMap = new ConcurrentHashMap<>();

    protected IEntityDtoServiceRelationMap getEntityDtoServiceRelationMap() {
        if(this.entityDtoServiceRelationMap == null) {
            this.entityDtoServiceRelationMap = this.applicationContext.getBean(IEntityDtoServiceRelationMap.class);
        }
        return this.entityDtoServiceRelationMap;
    };

    protected IUpdateService getUpdateService() {
        if(this.updateService == null) {
            this.updateService = applicationContext.getBean(IUpdateService.class);
        }
        return this.updateService;
    }

    protected ISelectService getSelectService() {
        if(this.selectService == null) {
            this.selectService = applicationContext.getBean(ISelectService.class);
        }
        return this.selectService;
    };

    protected DtoValidatorManager getDtoValidatorManager() {
        if(this.dtoValidatorManager == null) {
            this.dtoValidatorManager = applicationContext.getBean(DtoValidatorManager.class);
        }
        return this.dtoValidatorManager;
    };

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

    protected EntityDtoServiceRelation parseEntityDtoServiceRelation(String dtoName) throws ResourceNotFoundException {
        EntityDtoServiceRelation relationInfo = null;
        if(dtoNameMap.containsKey(dtoName)) {
            relationInfo = dtoNameMap.get(dtoName);
        } else {
            IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
            String dtoClassName = FieldUtils.parseClassName(dtoName);
            relationInfo = entityDtoServiceRelationMap.getByDtoName(dtoClassName);
            dtoNameMap.put(dtoName, relationInfo);
        }
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dtoName);
        }
        return relationInfo;
    }

    protected List<Object> convertFromMapList(List<Object> mapList, Class clazz) {
        List<Object> result = new ArrayList<>();
        for(Object map : mapList) {
            result.add(objectMapper.convertValue(map, clazz));
        }
        return result;
    }

    /*
     *** 通过ID获取实体类，转换成dto后返回
     *** 指定sub_entities参数可返回关联的子项
     *** 检索子项时使用与groupName相匹配的EntityFilter的条件
     *** 子项中不存在EntityFilter注解时，默认使用主类ID作为检索条件
     */
    public IServiceResult getById(String dtoName
            , Serializable id
            , String sub_entities) throws Exception {

        IServiceResult serviceResult = new DefaultServiceResult();
        EntityDtoServiceRelation relationInfo = parseEntityDtoServiceRelation(dtoName);
        Object data = getSelectService().getDtoById(relationInfo, id
                , ArrayParamUtils.convertStringToArray(sub_entities, ArrayParamUtils.DELIMITER_COMMA));
        serviceResult.setData(data);
        return serviceResult;
    }

    /*
     *** 读取dto中QueryField注解信息，组装成查询条件后查询实体列表，转化dto列表后返回
     *** page_index、page_size指定分页信息
     */
    public IServiceResult getPagesByDtoAnnotation(
            String dtoName
            , Integer page_index
            , Integer page_size
            , Map map) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();

        EntityDtoServiceRelation relationInfo = parseEntityDtoServiceRelation(dtoName);
//        ObjectMapper objectMapper = new ObjectMapper();
        // TODO: 忽略不存在的属性
        Object dto = objectMapper.convertValue(map, relationInfo.getDtoClass());

        if(page_index != null && page_size != null) {
            PageInfo pageInfo = getSelectService().getPagesByDtoAnnotation(relationInfo, dto
                    , page_index, page_size);

            serviceResult.setData(pageInfo);
        } else {
            List dataList = getSelectService().getListByDtoAnnotation(relationInfo, dto);
            serviceResult.setData(dataList);
        }

        return serviceResult;
    }

    /*
     *** QueryFieldModel设置查询条件后可查询实体列表，转化dto列表后返回
     *** page_index、page_size指定分页信息
     */
    public IServiceResult getPagesByQueryFields(
             String dtoName
            , Integer page_index
            , Integer page_size
            , List<QueryFieldModel> queryFiledModelList) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();

        EntityDtoServiceRelation relationInfo = parseEntityDtoServiceRelation(dtoName);
        if(page_index != null && page_size != null) {
            PageInfo pageInfo = getSelectService().getPagesByQueryFields(relationInfo
                    , queryFiledModelList, page_index, page_size);
            serviceResult.setData(pageInfo);
        } else {
            List dataList = getSelectService().getListByQueryFields(relationInfo, queryFiledModelList);
            serviceResult.setData(dataList);
        }

        return serviceResult;
    }

    public IServiceResult saveEntity(String dtoName
            , Object updateObject
            , String children
            , boolean updateChildrenOnly) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        Object dto = updateObject;
        if(updateObject instanceof Map) {
//            ObjectMapper objectMapper = new ObjectMapper();
            dto = objectMapper.convertValue(updateObject, relationInfo.getDtoClass());
        }
        return saveEntity(dtoName, dto,  relationInfo, children, updateChildrenOnly);
    }

    public IServiceResult saveEntity(String dtoName
            , Object updateObject
            , EntityDtoServiceRelation relation
            , String children
            , boolean updateChildrenOnly) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        return updateDto(dtoName, updateObject, relation, DefaultInsertEntityProvider.getInstance(), options, true);
    }

    public IServiceResult saveEntities(String dtoName
            , List<Object> mapList
            , String children
            , boolean updateChildrenOnly
            , boolean returnUpdateResult) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        Class actualClass = (Class)ClassUtils.getFirstTypeArgument(mapList.getClass());
        List<Object> objects = mapList;
        if(Map.class.isAssignableFrom(actualClass)) {
//            ObjectMapper objectMapper = new ObjectMapper();
            objects = convertFromMapList(mapList, relationInfo.getDtoClass());
        }
        return saveEntities(dtoName, objects, relationInfo, children, updateChildrenOnly, returnUpdateResult);
    }

    public IServiceResult saveEntities(String dtoName
            , List<Object> objects
            , EntityDtoServiceRelation relation
            , String children
            , boolean updateChildrenOnly
            , boolean returnUpdateResult) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);

        return updateDto(dtoName, objects, relation, DefaultInsertEntityProvider.getInstance()
                , options, returnUpdateResult);
    }

    public IServiceResult updateEntity(String dtoName
            , Object updateObject
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        Object dto = updateObject;
        if(updateObject instanceof Map) {
//            ObjectMapper objectMapper = new ObjectMapper();
            dto = objectMapper.convertValue(updateObject, relationInfo.getDtoClass());
        }

        return updateEntity(dtoName, dto, relationInfo
                , children, updateChildrenOnly, removeAndInsertNewChild, physicDelete);
    }

    public IServiceResult updateEntity(String dtoName
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

        return updateDto(dtoName, updateObject, relation, DefaultUpdateEntityProvider.getInstance(), options, true);
    }

    public IServiceResult updateEntities(String dtoName
            , List<Object> mapList
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete
            , boolean returnUpdateResult) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        Class actualClass = (Class)ClassUtils.getFirstTypeArgument(mapList.getClass());
        List<Object> objects = mapList;
        if(Map.class.isAssignableFrom(actualClass)) {
//            ObjectMapper objectMapper = new ObjectMapper();
            objects = convertFromMapList(mapList, relationInfo.getDtoClass());
        }

        return updateEntities(dtoName, objects, relationInfo, children
                , updateChildrenOnly, removeAndInsertNewChild, physicDelete, returnUpdateResult);
    }

    public IServiceResult updateEntities(String dtoName
            , List<Object> objects
            , EntityDtoServiceRelation relation
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete
            , boolean returnUpdateResult) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_AND_INSERT, removeAndInsertNewChild);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);

        return updateDto(dtoName, objects, relation, DefaultUpdateEntityProvider.getInstance()
                , options, returnUpdateResult);
    }

    public IServiceResult deleteById(String dtoName, String children
            , Serializable id
            , boolean updateChildrenOnly
            , boolean physicDelete) throws Exception  {
        return deleteByIds(dtoName, ArrayParamUtils.convertStringToArray(children), Collections.singleton(id)
                , updateChildrenOnly, physicDelete);
    }

    public IServiceResult deleteByIds(String dtoName
            , String[] children
            , Set<Serializable> ids
            , boolean updateChildrenOnly
            , boolean physicDelete) throws Exception  {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);

        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, children);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);

        return updateDto(dtoName, ids, relationInfo, DefaultDeleteEntityProvider.getInstance()
                , options, false);
    }

    public IServiceResult updateDto(
            String dtoName, Object dtoObject
            , String updateEntityProviderName, Map options, boolean returnUpdateResult) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        IUpdateEntityProvider updateEntityProvider = parseProviderByName(updateEntityProviderName);
        return updateDto(dtoName, relationInfo, updateEntityProvider, options, returnUpdateResult);
    }

    public IServiceResult updateDto(
            String dtoName, Object dtoObject
            , IUpdateEntityProvider updateEntityProvider, Map options, boolean returnUpdateResult) throws Exception {
        EntityDtoServiceRelation relationInfo = this.parseEntityDtoServiceRelation(dtoName);
        return updateDto(dtoName, relationInfo, updateEntityProvider, options, returnUpdateResult);
    }

    public IServiceResult updateDto(
            String dtoName, Object dtoObject, EntityDtoServiceRelation relationInfo
            , IUpdateEntityProvider updateEntityProvider, Map options, boolean returnUpdateResult) throws Exception {

        IServiceResult serviceResult = new DefaultServiceResult();
        BindException errors = null;
        try {
            IUpdateService crudService = getUpdateService();
            DtoValidatorManager dtoValidatorManager = this.getDtoValidatorManager();
            errors = new BindException(dtoObject, dtoName);
            dtoValidatorManager.validate(dtoObject, updateEntityProvider, errors);
            if(errors.hasErrors()) {
                throw new ValidateException("validate failed");
            }
            Collection<Object> updatedEntities = crudService.updateByProviders(relationInfo
                    , dtoObject, updateEntityProvider, options);
            if(returnUpdateResult) {
                serviceResult.setData(updatedEntities);
            }
        } catch (ValidateException ex) {
            serviceResult.setData(null);
            serviceResult.setGlobalErrorList(errors.getGlobalErrors());
            serviceResult.setFieldErrorList(errors.getFieldErrors());
        } catch (Exception ex) {
            serviceResult.setData(null);
            throw ex;
        } finally {
        }
        return serviceResult;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
