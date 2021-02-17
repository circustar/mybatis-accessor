package com.circustar.mvcenhance.support;

import com.circustar.mvcenhance.error.ResourceNotFoundException;
import com.circustar.mvcenhance.annotation.QueryFieldModel;
import com.circustar.mvcenhance.error.ValidateException;
import com.circustar.mvcenhance.response.DefaultServiceResult;
import com.circustar.mvcenhance.response.IServiceResult;
import com.circustar.mvcenhance.response.PageInfo;
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
import java.util.stream.Collectors;

public class ControllerSupport implements ApplicationContextAware {
    protected IEntityDtoServiceRelationMap entityDtoServiceRelationMap = null;
    protected ApplicationContext applicationContext;
    protected IUpdateService updateService = null;
    protected ISelectService selectService = null;
    protected Map<String, IUpdateEntityProvider> providerMap = new HashMap<>();
    protected DtoValidatorManager dtoValidatorManager = null;

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

    protected IUpdateEntityProvider getProviderByName(String updateProvidersName) {
        if(providerMap.containsKey(updateProvidersName)) {
            return providerMap.get(updateProvidersName);
        }
        IUpdateEntityProvider provider = null;
        if(applicationContext.containsBean(updateProvidersName)) {
            provider =  (IUpdateEntityProvider)applicationContext.getBean(updateProvidersName);
        }
        providerMap.put(updateProvidersName, provider);
        return provider;

    }

    /*
     *** 通过ID获取实体类，转换成dto后返回
     *** 指定sub_entities参数可返回关联的子项
     *** GroupName默认为空
     */
    public IServiceResult getById(String dto_name
            , Serializable id
            , String sub_entities) throws Exception {
        return getById(dto_name, id, sub_entities, "");
    }

    /*
     *** 通过ID获取实体类，转换成dto后返回
     *** 指定sub_entities参数可返回关联的子项
     *** 检索子项时使用与groupName相匹配的EntityFilter的条件
     *** 子项中不存在EntityFilter注解时，默认使用主类ID作为检索条件
     */
    public IServiceResult getById(String dto_name
            , Serializable id
            , String sub_entities
            , String groupName) throws Exception {

        IServiceResult serviceResult = new DefaultServiceResult();
        if (groupName == null) {
            groupName = "";
        }

        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();

        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        Object data = getSelectService().getDtoById(relationInfo, id
                , ArrayParamUtils.convertStringToArray(sub_entities, ArrayParamUtils.DELIMITER_COMMA)
                , groupName);
        serviceResult.setData(data);
        return serviceResult;
    }

    /*
     *** 读取dto中QueryField注解信息，组装成查询条件后查询实体列表，转化dto列表后返回
     *** page_index、page_size指定分页信息
     */
    public IServiceResult getPagesByDtoAnnotation(
            String dto_name
            , Integer page_index
            , Integer page_size
            , Map map) throws Exception {
        return getPagesByDtoAnnotation(dto_name, page_index, page_size, map, "");
    }

    /*
     *** 读取dto中QueryField注解信息，组装成查询条件后查询实体列表，转化dto列表后返回
     *** page_index、page_size指定分页信息
     */
    public IServiceResult getPagesByDtoAnnotation(
            String dto_name
            , Integer page_index
            , Integer page_size
            , Map map
            , String queryGroup) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();
        if(queryGroup == null) {
            queryGroup = "";
        }

        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        // TODO: 忽略不存在的属性
        Object dto= objectMapper.convertValue(map, relationInfo.getDtoClass());

        if(page_index != null && page_size != null) {
            PageInfo pageInfo = getSelectService().getPagesByDtoAnnotation(relationInfo, dto
                    , queryGroup, page_index, page_size);

            serviceResult.setData(pageInfo);
        } else {
            List dataList = getSelectService().getListByDtoAnnotation(relationInfo, dto, queryGroup);
            serviceResult.setData(dataList);
        }

        return serviceResult;
    }

    /*
     *** QueryFieldModel设置查询条件后可查询实体列表，转化dto列表后返回
     *** page_index、page_size指定分页信息
     */
    public IServiceResult getPagesByQueryFields(
             String dto_name
            , Integer page_index
            , Integer page_size
            , List<QueryFieldModel> queryFiledModelList) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();

        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
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

    public IServiceResult saveEntity(String dto_name
            , Map map
            , String children
            , boolean updateChildrenOnly) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        return updateMap(map, dto_name, DefaultInsertEntityProvider.getInstance()
                , options, true);
    }

    public IServiceResult saveEntities(String dto_name
            , List<Map> mapList
            , String children
            , boolean updateChildrenOnly
            , boolean returnUpdateResult) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        return updateMapList(mapList, dto_name, DefaultInsertEntityProvider.getInstance()
                , options, returnUpdateResult);
    }

    public IServiceResult updateEntity(String dtoName
            , String children
            , Map map
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_AND_INSERT, removeAndInsertNewChild);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);

        return updateMap(map, dtoName, DefaultUpdateEntityProvider.getInstance()
                , options, true);
    }

    public IServiceResult updateEntities(String dto_name, String children
            , List<Map> mapList
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete
            , boolean returnUpdateResult) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_AND_INSERT, removeAndInsertNewChild);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);

        return updateMapList(mapList, dto_name, DefaultUpdateEntityProvider.getInstance()
                , options, returnUpdateResult);
    }

    public IServiceResult deleteById(String dto_name, String children
            , Serializable id
            , boolean updateChildrenOnly
            , boolean physicDelete) throws Exception  {
        return deleteByIds(dto_name, ArrayParamUtils.convertStringToArray(children), Collections.singleton(id)
                , updateChildrenOnly, physicDelete);
    }

    public IServiceResult deleteByIds(String dto_name
            , String[] children
            , Set<Serializable> ids
            , boolean updateChildrenOnly
            , boolean physicDelete) throws Exception  {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, children);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);

        return updateDto(ids, dto_name, DefaultDeleteEntityProvider.getInstance()
                , options, false);
    }

    public IServiceResult updateMapList(
            List<Map> mapList, String dto_name, IUpdateEntityProvider updateEntityProvider, Map options
            , boolean returnUpdateResult) throws Exception {
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Object entities = mapList.stream().map(x -> objectMapper.convertValue(x, relationInfo.getDtoClass())).collect(Collectors.toList());

        return updateDto(entities, dto_name, relationInfo, updateEntityProvider, options, returnUpdateResult);
    }

    public IServiceResult updateMap(
            Map map, String dto_name, String updateProvidersName, Map options
            , boolean returnUpdateResult) throws Exception {
        IUpdateEntityProvider updateEntityProvider = this.getProviderByName(updateProvidersName);
        if(updateEntityProvider == null) {
            throw new ResourceNotFoundException("update provider not found");
        }
        return updateMap(map, dto_name, updateEntityProvider, options, returnUpdateResult);
    }

    public IServiceResult updateMap(
            Map map, String dto_name, IUpdateEntityProvider updateEntityProvider, Map options
            , boolean returnUpdateResult) throws Exception {
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        return updateMap(map, dto_name,  relationInfo, updateEntityProvider, options, returnUpdateResult);
    }

    public IServiceResult updateMap(
            Map map, String dto_name, EntityDtoServiceRelation relationInfo
            , IUpdateEntityProvider updateEntityProvider, Map options
            , boolean returnUpdateResult) throws Exception {

        Object updateObject = (new ObjectMapper()).convertValue(map, relationInfo.getDtoClass());
        return updateDto(updateObject, dto_name,  relationInfo, updateEntityProvider, options
                , returnUpdateResult);
    }

    public IServiceResult updateDto(
            Object updateObject, String dto_name
            , IUpdateEntityProvider updateEntityProvider
            , Map options
            , boolean returnUpdateResult) throws Exception {

        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        return updateDto(updateObject, dto_name,  relationInfo, updateEntityProvider
                , options, returnUpdateResult);
    }

    public IServiceResult updateDto(
            Object updateObject, String dto_name, EntityDtoServiceRelation relationInfo
            , IUpdateEntityProvider updateEntityProvider, Map options, boolean returnUpdateResult) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();
        BindException errors = null;
        try {
            IUpdateService crudService = getUpdateService();
            DtoValidatorManager dtoValidatorManager = this.getDtoValidatorManager();
            errors = new BindException(updateObject, dto_name);
            dtoValidatorManager.validate(updateObject, updateEntityProvider, errors);
            if(errors.hasErrors()) {
                throw new ValidateException("validate failed");
            }
            Collection<Object> updatedEntities = crudService.updateByProviders(relationInfo
                    , updateObject, updateEntityProvider, options);
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
