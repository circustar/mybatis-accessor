package com.circustar.mvcenhance.enhance.controller;

import com.circustar.mvcenhance.common.error.ResourceNotFoundException;
import com.circustar.mvcenhance.common.query.QueryFieldModel;
import com.circustar.mvcenhance.common.response.DefaultServiceResult;
import com.circustar.mvcenhance.common.response.IServiceResult;
import com.circustar.mvcenhance.common.response.PageInfo;
import com.circustar.mvcenhance.enhance.field.DtoClassInfo;
import com.circustar.mvcenhance.enhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.enhance.service.ICrudService;
import com.circustar.mvcenhance.enhance.service.ISelectService;
import com.circustar.mvcenhance.enhance.update.UpdateSubEntityStrategy;
import com.circustar.mvcenhance.enhance.update.IUpdateEntityProvider;
import com.circustar.mvcenhance.enhance.update.ValidateException;
import com.circustar.mvcenhance.enhance.utils.ArrayParamUtils;
import com.circustar.mvcenhance.enhance.utils.EnhancedConversionService;
import com.circustar.mvcenhance.enhance.utils.FieldUtils;
import org.springframework.validation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public interface IControllerAdapter {
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

    default Validator getValidator() {
        return getApplicationContext().getBean(SmartValidator.class);
    };

    default EnhancedConversionService getEnhancedConversionService() {
        return getApplicationContext().getBean(EnhancedConversionService.class);
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

//    /*
//     *** 通过ID删除指定实体
//     *** sub_entities指定级联删除对象
//     *** 如果在mybatis-plus开启逻辑删除后，仍要执行物理删除，可将physic_delete置为true
//     */
//    default IServiceResult deleteById(
//             String dto_name
//            , Serializable id
//            , String sub_entities
//            , boolean physicDelete) throws Exception {
//        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
//        String entityName = FieldUtils.parseClassName(dto_name);
//        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
//        if (relationInfo == null) {
//            throw new ResourceNotFoundException(dto_name);
//        }
//
//        return defaultUpdateObject(id, dto_name, relationInfo
//                , new String[]{IUpdateEntityProvider.DELETE_BY_ID}
//                , new Object[]{sub_entities, physicDelete});
//    }

//    /*
//     *** 通过ids删除多个实体
//     *** sub_entities指定级联删除对象
//     *** 如果在mybatis-plus开启逻辑删除后，仍要执行物理删除，可将physic_delete置为true
//     */
//    default IServiceResult deleteByIds(
//             String dto_name
//            , List<Serializable> ids
//            , boolean physicDelete) throws Exception {
//        String entityName = FieldUtils.parseClassName(dto_name);
//        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
//        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
//        if (relationInfo == null) {
//            throw new ResourceNotFoundException(dto_name);
//        }
//        if(ids == null || ids.size() == 0) {
//            throw new Exception("ID not found");
//        }
//        Map<String, Object> map = new HashMap<>();
//
//        return updateByMap(dto_name, IUpdateEntityProvider.DELETE_LIST_BY_IDS
//                , new Object[]{physicDelete}, map);
//    }

//    /*
//     *** 保存一个实体
//     *** sub_entities指定级联保存对象
//     */
//    default IServiceResult save(
//             String dto_name
//            , Map map
//            , String sub_entities) throws Exception {
//        IServiceResult serviceResult = new DefaultServiceResult();
//        Validator validator = getValidator();
//        ICrudService crudService = getCrudService();
//        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
//        String entityName = FieldUtils.parseClassName(dto_name);
//        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
//        if (relationInfo == null) {
//            throw new ResourceNotFoundException(dto_name);
//        }
//        ObjectMapper objectMapper = new ObjectMapper();
//        Object o= objectMapper.convertValue(map, relationInfo.getDto());
//        BindException errors = new BindException(o, dto_name);
//        if(validator != null) {
//            validator.validate(o, errors);
//        }
//        if(errors.hasErrors()) {
//            serviceResult.addFieldErrorList(errors.getFieldErrors());
//            return serviceResult;
//        }
//        crudService.save(relationInfo, o
//                , FieldUtils.parseSubEntityNames(sub_entities));
//        serviceResult.setData(o);
//        return serviceResult;
//    }

//    /*
//     *** 保存多个实体
//     */
//    default IServiceResult saveList(
//            String dto_name
//            , List<Map> mapList) {
//        IServiceResult serviceResult = new DefaultServiceResult();
//        try {
//            ApplicationContext applicationContext = getApplicationContext();
//            Validator validator = getValidator();
//            ICrudService crudService = getCrudService();
//            IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
//            String entityName = FieldUtils.parseClassName(dto_name);
//            EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
//            if(relationInfo == null) {
//                serviceResult.setError(new ResourceNotFoundErrorInfo(dto_name));
//                return serviceResult;
//            }
//            ObjectMapper objectMapper = new ObjectMapper();
//            List<Object> objectList = new ArrayList<>();
//            List<FieldError> errorInfos = new ArrayList<>();
//            for(Map map : mapList) {
//                Object o = objectMapper.convertValue(map, relationInfo.getDto());
//                BindException errors = new BindException(o, dto_name);
//                if (validator != null) {
//                    validator.validate(o, errors);
//                }
//                if (errors.hasErrors()) {
//                    errorInfos.addAll(errors.getFieldErrors());
//                }
//                objectList.add(o);
//            }
//            if(errorInfos.size() > 0) {
//                serviceResult.setError(new FieldErrorInfo("数据校验出错", errorInfos));
//                return serviceResult;
//            }
//
//            boolean saveResult = crudService.saveList(relationInfo, objectList);
//            if(!saveResult) {
//                serviceResult.setError(new InsertFailureErrorInfo());
//            }
//
//        } catch (Exception ex) {
//            serviceResult.setError(new ExceptionErrorInfo(ex));
//        }
//        return serviceResult;
//    }

//    /*
//     *** 通过ID更新实体
//     *** subEntities指定更新级联对象
//     *** subEntityRemoveAndInsert为true时级联对象先删除再插入
//     *** subEntityPhysicDelete为true时物理删除
//     */
//    default IServiceResult update(
//             String dto_name
//            , Serializable id
//            , Map map
//            , String subEntityName
//            , boolean subEntityRemoveAndInsert
//            , boolean subEntityPhysicDelete) throws Exception {
//        IServiceResult serviceResult = new DefaultServiceResult();
//        ICrudService crudService = getCrudService();
//        Validator validator = getValidator();
//        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
//        String entityName = FieldUtils.parseClassName(dto_name);
//        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
//        if (relationInfo == null) {
//            throw new ResourceNotFoundException(dto_name);
//        }
//        ObjectMapper objectMapper = new ObjectMapper();
//        Object o= objectMapper.convertValue(map, relationInfo.getDto());
//
//        BindException errors = new BindException(o, dto_name);
//        if(validator != null) {
//            validator.validate(o, errors);
//        }
//        if(errors.hasErrors()) {
//            serviceResult.addFieldErrorList(errors.getFieldErrors());
//            return serviceResult;
//        }
//
//        UpdateSubEntityStrategy updateStrategy = subEntityRemoveAndInsert?
//                UpdateSubEntityStrategy.DELETE_BEFORE_INSERT : UpdateSubEntityStrategy.INSERT_OR_UPDATE;
//        List<String> updateSubEntityList = FieldUtils.parseSubEntityNames(subEntityName);
//
//        crudService.update(relationInfo, id, o
//                , updateSubEntityList, updateStrategy, subEntityPhysicDelete);
//        return serviceResult;
//    }

//    /*
//     *** 新增、修改或删除多个实体
//     *** physicDelete为true时物理删除
//     */
//    default IServiceResult saveOrUpdateOrDeleteList(
//            String dto_name
//            , List<Map> mapList
//            , boolean physicDelete) throws Exception {
//        IServiceResult serviceResult = new DefaultServiceResult();
//        ICrudService crudService = getCrudService();
//        Validator validator = getValidator();
//        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
//        String entityName = FieldUtils.parseClassName(dto_name);
//        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
//        if (relationInfo == null) {
//            throw new ResourceNotFoundException(dto_name);
//        }
//        ObjectMapper objectMapper = new ObjectMapper();
//        List<Object> objectList = new ArrayList<>();
//        List<FieldError> errorInfos = new ArrayList<>();
//        for(Map map : mapList) {
//            Object o = objectMapper.convertValue(map, relationInfo.getDto());
//            BindException errors = new BindException(o, dto_name);
//            if (validator != null) {
//                validator.validate(o, errors);
//            }
//            if (errors.hasErrors()) {
//                serviceResult.addFieldErrorList(errors.getFieldErrors());
//            }
//            objectList.add(o);
//        }
//        if(serviceResult.containValidateErrors()) {
//            return serviceResult;
//        }
//
//        crudService.saveOrUpdateOrDeleteList(relationInfo, objectList, physicDelete);
//
//        return serviceResult;
//    }

//    /*
//     *** 只更新一个实体（通过id指定）的级联对象
//     *** subEntityName指定更新级联对象
//     *** subEntityRemoveAndInsert为true时级联对象先删除再插入
//     *** subEntityPhysicDelete为true时物理删除
//     */
//    default IServiceResult updateSubEntities(
//            String dto_name
//            , Serializable id
//            , List<Map> mapList
//            , String subEntityName
//            , boolean subEntityRemoveAndInsert
//            , boolean subEntityPhysicDelete) throws Exception {
//        IServiceResult serviceResult = new DefaultServiceResult();
//        ICrudService crudService = getCrudService();
//        Validator validator = getValidator();
//        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
//        String entityName = FieldUtils.parseClassName(dto_name);
//        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
//        if (relationInfo == null) {
//            throw new ResourceNotFoundException(dto_name);
//        }
//        List<SubFieldInfo> subDtoList = SubFieldInfo.getSubFieldInfoList(entityDtoServiceRelationMap
//                , relationInfo, Collections.singletonList(subEntityName));
//        if(subDtoList == null || subDtoList.size() == 0) {
//            throw new ResourceNotFoundException(subEntityName);
//        }
//        EntityDtoServiceRelation subFiledRelationInfo = entityDtoServiceRelationMap.getByDtoClass((Class)subDtoList.get(0).getFieldInfo().getActualType());
//        if(subFiledRelationInfo == null) {
//            throw new ResourceNotFoundException(subEntityName);
//        }
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        List<Object> objectList = new ArrayList<>();
//        for(Map map : mapList) {
//            Object o = objectMapper.convertValue(map, subFiledRelationInfo.getDto());
//            BindException errors = new BindException(o, dto_name);
//            if (validator != null) {
//                validator.validate(o, errors);
//            }
//            if (errors.hasErrors()) {
//                serviceResult.addFieldErrorList(errors.getFieldErrors());
//            }
//            objectList.add(o);
//        }
//        if(serviceResult.containValidateErrors()) {
//            return serviceResult;
//        }
//
//        UpdateSubEntityStrategy updateStrategy = subEntityRemoveAndInsert?
//                UpdateSubEntityStrategy.DELETE_BEFORE_INSERT : UpdateSubEntityStrategy.INSERT_OR_UPDATE;
//
//        crudService.updateSubEntityList(relationInfo, id, subFiledRelationInfo
//                , objectList, updateStrategy, subEntityPhysicDelete);
//        return serviceResult;
//    }

//    default IServiceResult defaultUpdate(
//            String dto_name, String[] updateNames, Object[] options
//            , Map map) throws Exception {
//
//        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
//        String entityName = FieldUtils.parseClassName(dto_name);
//        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
//        if (relationInfo == null) {
//            throw new ResourceNotFoundException(dto_name);
//        }
//        Object updateObject = (new ObjectMapper()).convertValue(map, relationInfo.getDto());
//
//        return defaultUpdateObject(updateObject, dto_name,  relationInfo, updateNames, options);
//    }

    default IServiceResult deleteById(String dto_name
            , String id
            , String sub_entities
            , Boolean physic_delete) throws Exception {
        return defaultUpdateObject(id, dto_name, new String[]{IUpdateEntityProvider.DELETE_BY_ID}
                , new Object[]{sub_entities, physic_delete==null?false:physic_delete});
    }

    default IServiceResult removeByIds(String dto_name
            , List<Serializable> ids
            , Boolean physic_delete) throws Exception  {
        return defaultUpdateObject(ids, dto_name, new String[]{IUpdateEntityProvider.DELETE_LIST_BY_IDS}
                , new Object[]{physic_delete==null?false:physic_delete});
    }

    default IServiceResult saveEntity(String dto_name
            , Map map
            , String sub_entities) throws Exception {
        return defaultUpdateMap(map, dto_name, new String[]{IUpdateEntityProvider.INSERT}, null);
    }

    default IServiceResult updateEntity(String dto_name
            , Map map
            , String subEntities
            , Boolean remove_and_insert
            , Boolean physic_delete) throws Exception {
        return defaultUpdateMap(map, dto_name, new String[]{IUpdateEntityProvider.UPDATE}
                , new Object[]{remove_and_insert? UpdateSubEntityStrategy.DELETE_BEFORE_INSERT:UpdateSubEntityStrategy.INSERT_OR_UPDATE
                        , physic_delete==null?false:physic_delete});
    }

    default IServiceResult saveOrUpdateOrDeleteEntities(String dto_name
            , List<Map> mapList
            , Boolean physicDelete) throws Exception {
        return defaultUpdateMapList(mapList, dto_name, new String[]{IUpdateEntityProvider.SAVE_UPDATE_DELETE_LIST}
                , new Object[]{physicDelete==null?false:physicDelete});
    }

    default IServiceResult updateSubEntities(String dto_name
            , Map map
            , String subEntities
            , Boolean remove_and_insert
            , Boolean physic_delete) throws Exception {
        return defaultUpdateMap(map, dto_name, new String[]{IUpdateEntityProvider.UPDATE_SUB_ENTITIES}
                , new Object[]{remove_and_insert? UpdateSubEntityStrategy.DELETE_BEFORE_INSERT:UpdateSubEntityStrategy.INSERT_OR_UPDATE
                        , physic_delete==null?false:physic_delete});
    }

    default IServiceResult defaultUpdateMapList(
            List<Map> mapList, String dto_name, String[] updateNames, Object[] options) throws Exception {
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Object entities = mapList.stream().map(x -> objectMapper.convertValue(x, relationInfo.getDto())).collect(Collectors.toList());

        return defaultUpdateObject(entities, dto_name, relationInfo, updateNames, options);
    }

    default IServiceResult defaultUpdateMap(
            Map map, String dto_name, String[] updateNames, Object[] options) throws Exception {
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        return defaultUpdateMap(map, dto_name,  relationInfo, updateNames, options);
    }

    default IServiceResult defaultUpdateMap(
            Map map, String dto_name, EntityDtoServiceRelation relationInfo
            , String[] updateNames, Object[] options) throws Exception {

        Object updateObject = (new ObjectMapper()).convertValue(map, relationInfo.getDto());
        return defaultUpdateObject(updateObject, dto_name,  relationInfo, updateNames, options);
    }

    default IServiceResult defaultUpdateObject(
            Object updateObject, String dto_name, String[] updateNames, Object[] options) throws Exception {

        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        return defaultUpdateObject(updateObject, dto_name,  relationInfo, updateNames, options);
    }

    default IServiceResult defaultUpdateObject(
            Object updateObject, String dto_name, EntityDtoServiceRelation relationInfo
            , String[] updateNames, Object[] options) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();
        BindException errors = null;
        try {
            ICrudService crudService = getCrudService();
            errors = new BindException(updateObject, dto_name);
            List<Object> updatedEntities = crudService.updateByProviders(relationInfo
                    , updateObject, updateNames, options, errors);
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
