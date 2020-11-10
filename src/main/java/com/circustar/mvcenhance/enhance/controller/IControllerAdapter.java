package com.circustar.mvcenhance.enhance.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.common.error.ResourceNotFoundException;
import com.circustar.mvcenhance.common.query.EntityFilter;
import com.circustar.mvcenhance.common.query.QueryFieldModel;
import com.circustar.mvcenhance.common.response.DefaultServiceResult;
import com.circustar.mvcenhance.common.response.IServiceResult;
import com.circustar.mvcenhance.common.response.PageInfo;
import com.circustar.mvcenhance.enhance.field.SubFieldInfo;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.enhance.service.ICrudService;
import com.circustar.mvcenhance.enhance.update.UpdateSubEntityStrategy;
import com.circustar.mvcenhance.enhance.update.IUpdateObjectProvider;
import com.circustar.mvcenhance.enhance.update.UpdateEntity;
import com.circustar.mvcenhance.enhance.utils.EnhancedConversionService;
import com.circustar.mvcenhance.enhance.utils.FieldUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

public interface IControllerAdapter {
    ApplicationContext getApplicationContext();

    default IEntityDtoServiceRelationMap getEntityDtoServiceRelationMap() {
        return getApplicationContext().getBean(IEntityDtoServiceRelationMap.class);
    };

    default ICrudService getCrudService() {
        return getApplicationContext().getBean(ICrudService.class);
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

        ApplicationContext applicationContext = getApplicationContext();
        EnhancedConversionService converter = getEnhancedConversionService();
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();

        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
//            Class serviceName = relationInfo.getService();
        IService s = (IService) applicationContext.getBean(relationInfo.getService());
        Object oriEntity = s.getById(id);
        if (oriEntity == null) {
            return serviceResult;
        }
        Object dto = converter.convert(oriEntity, relationInfo.getDto());

        serviceResult.setData(dto);
        if (dto == null || StringUtils.isEmpty(sub_entities)) {
            return serviceResult;
        }

        List<String> subEntityList = FieldUtils.parseSubEntityNames(sub_entities);
        List<Field> subFields = FieldUtils.getExistFields(dto, subEntityList, false);

        Map<String, EntityFilter[]> tableJoinerMap = new HashMap<>();
        List<String> noAnnotationInfoList = new ArrayList<>();
        FieldUtils.parseFieldAnnotationToMap(subFields, EntityFilter.class
                , tableJoinerMap, noAnnotationInfoList);

        String keyColumn = TableInfoHelper.getTableInfo(relationInfo.getEntity()).getKeyColumn();
        SubFieldInfo.setSubDtoAfterQueryById(applicationContext, converter, entityDtoServiceRelationMap
                , relationInfo, dto, noAnnotationInfoList, keyColumn, id);

        SubFieldInfo.setSubDtoAfterQueryByTableJoiner(applicationContext, converter, entityDtoServiceRelationMap
                , relationInfo, dto, tableJoinerMap, groupName);
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

        ApplicationContext applicationContext = getApplicationContext();
        EnhancedConversionService converter = getEnhancedConversionService();
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        Class serviceName = relationInfo.getService();
        IService s = (IService)applicationContext.getBean(serviceName);

        ObjectMapper objectMapper = new ObjectMapper();
        Object dto= objectMapper.convertValue(map, relationInfo.getDto());

        QueryWrapper qw = new QueryWrapper();

        List<QueryFieldModel> queryFiledModelList = QueryFieldModel.getQueryFieldModeFromDto(dto, queryGroup);
        QueryFieldModel.setQueryWrapper(queryFiledModelList, qw);

        if(page_index != null && page_size != null){
            Page page = new Page(page_index, page_size);
            IPage pageInfo = s.page(page, qw);

            List dtoList = converter.convertList(pageInfo.getRecords(), relationInfo.getDto());
            serviceResult.setData(dtoList);
            serviceResult.setPageInfo(new PageInfo(pageInfo.getTotal(), pageInfo.getSize(), pageInfo.getCurrent()));
        } else {
            List dtoList = converter.convertList(s.list(qw), relationInfo.getDto());
            serviceResult.setData(dtoList);
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
        ApplicationContext applicationContext = getApplicationContext();
        EnhancedConversionService converter = getEnhancedConversionService();
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        Class serviceName = relationInfo.getService();
        IService s = (IService)applicationContext.getBean(serviceName);

        QueryWrapper qw = new QueryWrapper();

        QueryFieldModel.setQueryWrapper(queryFiledModelList, qw);

        if(page_index != null && page_size != null){
            Page page = new Page(page_index, page_size);
            IPage pageInfo = s.page(page, qw);

            List dtoList = converter.convertList(pageInfo.getRecords(), relationInfo.getDto());
            serviceResult.setData(dtoList);
            serviceResult.setPageInfo(new PageInfo(pageInfo.getTotal(), pageInfo.getSize(), pageInfo.getCurrent()));
        } else {
            List dtoList = converter.convertList(s.list(qw), relationInfo.getDto());
            serviceResult.setData(dtoList);
        }
        return serviceResult;
    }

    /*
     *** 通过ID删除指定实体
     *** sub_entities指定级联删除对象
     *** 如果在mybatis-plus开启逻辑删除后，仍要执行物理删除，可将physic_delete置为true
     */
    default IServiceResult deleteById(
             String dto_name
            , Serializable id
            , String sub_entities
            , boolean physicDelete) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        ICrudService crudService = getCrudService();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        String keyColumn = TableInfoHelper.getTableInfo(relationInfo.getEntity()).getKeyColumn();

        crudService.deleteById(relationInfo, keyColumn, id
                , FieldUtils.parseSubEntityNames(sub_entities), physicDelete);
        return serviceResult;
    }

    /*
     *** 通过ids删除多个实体
     *** sub_entities指定级联删除对象
     *** 如果在mybatis-plus开启逻辑删除后，仍要执行物理删除，可将physic_delete置为true
     */
    default IServiceResult deleteByIds(
             String dto_name
            , List<Serializable> ids
            , String sub_entities
            , boolean physicDelete) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();
        ICrudService crudService = getCrudService();
        String entityName = FieldUtils.parseClassName(dto_name);
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        String keyColumn = TableInfoHelper.getTableInfo(relationInfo.getEntity()).getKeyColumn();

        crudService.deleteByIds(relationInfo, keyColumn, ids
                , FieldUtils.parseSubEntityNames(sub_entities), physicDelete);
        return serviceResult;
    }

    /*
     *** 保存一个实体
     *** sub_entities指定级联保存对象
     */
    default IServiceResult save(
             String dto_name
            , Map map
            , String sub_entities) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();
        Validator validator = getValidator();
        ICrudService crudService = getCrudService();
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Object o= objectMapper.convertValue(map, relationInfo.getDto());
        BindException errors = new BindException(o, dto_name);
        if(validator != null) {
            validator.validate(o, errors);
        }
        if(errors.hasErrors()) {
            serviceResult.addFieldErrorList(errors.getFieldErrors());
            return serviceResult;
        }
        crudService.save(relationInfo, o
                , FieldUtils.parseSubEntityNames(sub_entities));
        serviceResult.setData(o);
        return serviceResult;
    }

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

    /*
     *** 通过ID更新实体
     *** subEntities指定更新级联对象
     *** subEntityRemoveAndInsert为true时级联对象先删除再插入
     *** subEntityPhysicDelete为true时物理删除
     */
    default IServiceResult update(
             String dto_name
            , Serializable id
            , Map map
            , String subEntityName
            , boolean subEntityRemoveAndInsert
            , boolean subEntityPhysicDelete) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();
        ICrudService crudService = getCrudService();
        Validator validator = getValidator();
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Object o= objectMapper.convertValue(map, relationInfo.getDto());

        BindException errors = new BindException(o, dto_name);
        if(validator != null) {
            validator.validate(o, errors);
        }
        if(errors.hasErrors()) {
            serviceResult.addFieldErrorList(errors.getFieldErrors());
            return serviceResult;
        }

        UpdateSubEntityStrategy updateStrategy = subEntityRemoveAndInsert?
                UpdateSubEntityStrategy.DELETE_BEFORE_INSERT : UpdateSubEntityStrategy.INSERT_OR_UPDATE_OR_DELETE;
        List<String> updateSubEntityList = FieldUtils.parseSubEntityNames(subEntityName);

        crudService.update(relationInfo, id, o
                , updateSubEntityList, updateStrategy, subEntityPhysicDelete);
        return serviceResult;
    }

    /*
     *** 新增、修改或删除多个实体
     *** physicDelete为true时物理删除
     */
    default IServiceResult saveOrUpdateOrDeleteList(
            String dto_name
            , List<Map> mapList
            , boolean physicDelete) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();
        ICrudService crudService = getCrudService();
        Validator validator = getValidator();
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        List<Object> objectList = new ArrayList<>();
        List<FieldError> errorInfos = new ArrayList<>();
        for(Map map : mapList) {
            Object o = objectMapper.convertValue(map, relationInfo.getDto());
            BindException errors = new BindException(o, dto_name);
            if (validator != null) {
                validator.validate(o, errors);
            }
            if (errors.hasErrors()) {
                serviceResult.addFieldErrorList(errors.getFieldErrors());
            }
            objectList.add(o);
        }
        if(serviceResult.containValidateErrors()) {
            return serviceResult;
        }

        crudService.saveOrUpdateOrDeleteList(relationInfo, objectList, physicDelete);

        return serviceResult;
    }

    /*
     *** 只更新一个实体（通过id指定）的级联对象
     *** subEntityName指定更新级联对象
     *** subEntityRemoveAndInsert为true时级联对象先删除再插入
     *** subEntityPhysicDelete为true时物理删除
     */
    default IServiceResult updateSubEntities(
            String dto_name
            , Serializable id
            , List<Map> mapList
            , String subEntityName
            , boolean subEntityRemoveAndInsert
            , boolean subEntityPhysicDelete) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();
        ICrudService crudService = getCrudService();
        Validator validator = getValidator();
        IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
        String entityName = FieldUtils.parseClassName(dto_name);
        EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
        if (relationInfo == null) {
            throw new ResourceNotFoundException(dto_name);
        }
        List<SubFieldInfo> subDtoList = SubFieldInfo.getSubFieldInfoList(entityDtoServiceRelationMap
                , relationInfo, Collections.singletonList(subEntityName));
        if(subDtoList == null || subDtoList.size() == 0) {
            throw new ResourceNotFoundException(subEntityName);
        }
        EntityDtoServiceRelation subFiledRelationInfo = entityDtoServiceRelationMap.getByDtoClass((Class)subDtoList.get(0).getFieldInfo().getActualType());
        if(subFiledRelationInfo == null) {
            throw new ResourceNotFoundException(subEntityName);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<Object> objectList = new ArrayList<>();
        for(Map map : mapList) {
            Object o = objectMapper.convertValue(map, subFiledRelationInfo.getDto());
            BindException errors = new BindException(o, dto_name);
            if (validator != null) {
                validator.validate(o, errors);
            }
            if (errors.hasErrors()) {
                serviceResult.addFieldErrorList(errors.getFieldErrors());
            }
            objectList.add(o);
        }
        if(serviceResult.containValidateErrors()) {
            return serviceResult;
        }

        UpdateSubEntityStrategy updateStrategy = subEntityRemoveAndInsert?
                UpdateSubEntityStrategy.DELETE_BEFORE_INSERT : UpdateSubEntityStrategy.INSERT_OR_UPDATE_OR_DELETE;

        crudService.updateSubEntityList(relationInfo, id, subFiledRelationInfo
                , objectList, updateStrategy, subEntityPhysicDelete);
        return serviceResult;
    }

    default IServiceResult businessUpdate(
             String dto_name
            , Map map) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();
        IUpdateObjectProvider provider = null;
        try {
            IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
            ApplicationContext applicationContext = getApplicationContext();
            ICrudService crudService = getCrudService();
            Validator validator = getValidator();
            String entityName = FieldUtils.parseClassName(dto_name);
            EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
            if (relationInfo == null) {
                throw new ResourceNotFoundException(dto_name);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            Object o= objectMapper.convertValue(map, relationInfo.getDto());

            BindException errors = new BindException(o, dto_name);
            if(validator != null) {
                validator.validate(o, errors);
            }
            if (errors.hasErrors()) {
                serviceResult.addFieldErrorList(errors.getFieldErrors());
                return serviceResult;
            }
            provider = applicationContext.getBean(relationInfo.getUpdateObjectProvider());
            provider.validateBeforeUpdate(o, errors);
            if (errors.hasErrors()) {
                serviceResult.addFieldErrorList(errors.getFieldErrors());
                return serviceResult;
            }

            List<UpdateEntity> entities = provider.createUpdateEntities(o);
            crudService.businessUpdate(entities);
            provider.onSuccess();
        } catch (Exception ex) {
            serviceResult.setData(null);
            provider.onException(ex);
            throw ex;
        } finally {
            provider.onEnd();
        }
        return serviceResult;
    }
}
