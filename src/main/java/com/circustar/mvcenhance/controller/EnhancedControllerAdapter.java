package com.circustar.mvcenhance.controller;

import com.circustar.mvcenhance.error.ResourceNotFoundException;
import com.circustar.mvcenhance.annotation.QueryFieldModel;
import com.circustar.mvcenhance.error.ValidateException;
import com.circustar.mvcenhance.response.DefaultServiceResult;
import com.circustar.mvcenhance.response.IServiceResult;
import com.circustar.mvcenhance.response.PageInfo;
import com.circustar.mvcenhance.utils.MvcEnhanceConstants;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.service.ICrudService;
import com.circustar.mvcenhance.service.ISelectService;
import com.circustar.mvcenhance.provider.*;
import com.circustar.mvcenhance.utils.ArrayParamUtils;
import com.circustar.mvcenhance.utils.FieldUtils;
import org.springframework.validation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public interface EnhancedControllerAdapter {
    ApplicationContext getApplicationContext();

    default IEntityDtoServiceRelationMap getEntityDtoServiceRelationMap() {
        return getApplicationContext().getBean(IEntityDtoServiceRelationMap.class);
    };

    default ICrudService getCrudService() {
        return getApplicationContext().getBean(ICrudService.class);
    };

    default ISelectService getSelectService() {
        return getApplicationContext().getBean(ISelectService.class);
    };

    /*
     *** 通过ID获取实体类，转换成dto后返回
     *** 指定sub_entities参数可返回关联的子项
     *** GroupName默认为空
     */
    default IServiceResult getById(String dto_name
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
    default IServiceResult getById(String dto_name
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
    default IServiceResult getPagesByDtoAnnotation(
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
    default IServiceResult getPagesByDtoAnnotation(
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
        Object dto= objectMapper.convertValue(map, relationInfo.getDto());

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
    default IServiceResult getPagesByQueryFields(
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

    default IServiceResult saveEntity(String dto_name
            , Map map
            , String[] subEntities
            , boolean updateChildrenOnly) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, subEntities);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        return defaultUpdateMap(map, dto_name, new IUpdateEntityProvider[]{DefaultInsertEntitiesProvider.getInstance()}
                , options);
    }

    default IServiceResult saveEntities(String dto_name
            , List<Map> mapList
            , String[] subEntities
            , boolean updateChildrenOnly) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, subEntities);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        return defaultUpdateMapList(mapList, dto_name, new IUpdateEntityProvider[]{DefaultInsertEntitiesProvider.getInstance()}
                , options);
    }

    default IServiceResult updateEntity(String dtoName
            , String[] children
            , Map map
            , boolean updateChildrenOnly
            , boolean remove_and_insert
            , boolean physicDelete) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, children);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_BEFORE_UPDATE, remove_and_insert);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);

        return defaultUpdateMap(map, dtoName, new IUpdateEntityProvider[]{DefaultUpdateEntityProvider.getInstance()}
                , options);
    }

    default IServiceResult updateEntities(String dto_name, String[] children
            , List<Map> mapList
            , boolean updateChildrenOnly
            , boolean remove_and_insert
            , boolean physicDelete) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, children);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_BEFORE_UPDATE, remove_and_insert);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);

        return defaultUpdateMapList(mapList, dto_name, new IUpdateEntityProvider[]{DefaultUpdateEntityProvider.getInstance()}
                , options);
    }

    default IServiceResult deleteById(String dto_name, String[] children
            , Serializable id
            , boolean updateChildrenOnly
            , Boolean physicDelete) throws Exception  {
        return deleteByIds(dto_name, children, Collections.singleton(id), updateChildrenOnly, physicDelete);
    }

    default IServiceResult deleteByIds(String dto_name
            , String[] children
            , Set<Serializable> ids
            , boolean updateChildrenOnly
            , Boolean physicDelete) throws Exception  {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_SUB_ENTITY_LIST, children);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);

        return defaultUpdateObject(ids, dto_name, new IUpdateEntityProvider[]{DefaultDeleteEntitiesProvider.getInstance()}
                , options);
    }

    default IServiceResult defaultUpdateMapList(
            List<Map> mapList, String dto_name, IUpdateEntityProvider[] updateEntityProviders, Map options) throws Exception {
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Object entities = mapList.stream().map(x -> objectMapper.convertValue(x, relationInfo.getDto())).collect(Collectors.toList());

        return defaultUpdateObject(entities, dto_name, relationInfo, updateEntityProviders, options);
    }

    default IServiceResult defaultUpdateMap(
            Map map, String dto_name, String[] updateProvidersNames, Map options) throws Exception {
        List<IUpdateEntityProvider> updateEntityProviders = Arrays.stream(updateProvidersNames).map(x -> (IUpdateEntityProvider)getApplicationContext().getBean(x))
                .collect(Collectors.toList());
        if(updateEntityProviders == null || updateEntityProviders.size() == 0) {
            throw new ResourceNotFoundException("update provider not found");
        }
        IUpdateEntityProvider[] iUpdateEntityProviders = updateEntityProviders.toArray(new IUpdateEntityProvider[updateEntityProviders.size()]);
        return defaultUpdateMap(map, dto_name, iUpdateEntityProviders, options);
    }

    default IServiceResult defaultUpdateMap(
            Map map, String dto_name, IUpdateEntityProvider[] updateEntityProviders, Map options) throws Exception {
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        return defaultUpdateMap(map, dto_name,  relationInfo, updateEntityProviders, options);
    }

    default IServiceResult defaultUpdateMap(
            Map map, String dto_name, EntityDtoServiceRelation relationInfo
            , IUpdateEntityProvider[] updateEntityProviders, Map options) throws Exception {

        Object updateObject = (new ObjectMapper()).convertValue(map, relationInfo.getDto());
        return defaultUpdateObject(updateObject, dto_name,  relationInfo, updateEntityProviders, options);
    }

    default IServiceResult defaultUpdateObject(
            Object updateObject, String dto_name
            , IUpdateEntityProvider[] updateEntityProviders, Map options) throws Exception {

        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        return defaultUpdateObject(updateObject, dto_name,  relationInfo, updateEntityProviders, options);
    }

    default IServiceResult defaultUpdateObject(
            Object updateObject, String dto_name, EntityDtoServiceRelation relationInfo
            , IUpdateEntityProvider[] updateEntityProviders, Map options) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();
        BindException errors = null;
        try {
            ICrudService crudService = getCrudService();
            errors = new BindException(updateObject, dto_name);
            Collection<Object> updatedEntities = crudService.updateByProviders(relationInfo
                    , updateObject, updateEntityProviders, options, errors);
            serviceResult.setData(updatedEntities);
        } catch (ValidateException ex) {
            serviceResult.setData(null);
            serviceResult.setFieldErrorList(errors.getFieldErrors());
        } catch (Exception ex) {
            serviceResult.setData(null);
            throw ex;
        } finally {
        }
        return serviceResult;
    }



}
