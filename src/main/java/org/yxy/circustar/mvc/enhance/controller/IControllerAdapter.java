package org.yxy.circustar.mvc.enhance.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;
import org.yxy.circustar.mvc.common.error.*;
import org.yxy.circustar.mvc.common.query.Join;
import org.yxy.circustar.mvc.common.response.DefaultServiceResult;
import org.yxy.circustar.mvc.common.response.IServiceResult;
import org.yxy.circustar.mvc.common.response.PageInfo;
import org.yxy.circustar.mvc.common.query.QueryFieldModel;
import org.yxy.circustar.mvc.enhance.relation.EntityDtoServiceRelation;
import org.yxy.circustar.mvc.enhance.relation.IEntityDtoServiceRelationMap;
import org.yxy.circustar.mvc.enhance.service.ICrudService;
import org.yxy.circustar.mvc.enhance.update.CascadeUpdateStrategy;
import org.yxy.circustar.mvc.enhance.update.IUpdateObjectProvider;
import org.yxy.circustar.mvc.enhance.update.UpdateEntity;
import org.yxy.circustar.mvc.enhance.utils.FieldUtils;
import org.yxy.circustar.mvc.enhance.utils.EnhancedConversionService;
import org.yxy.circustar.mvc.enhance.field.SubFieldInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    default IServiceResult getById(String dto_name
            , Serializable id
            , String sub_entities
            , String groupName) {

        IServiceResult serviceResult = new DefaultServiceResult();
        try {
            if (groupName == null) {
                groupName = "";
            }

            ApplicationContext applicationContext = getApplicationContext();
            EnhancedConversionService converter = getEnhancedConversionService();
            IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();

            String entityName = FieldUtils.parseClassNameFromPathVariable(dto_name);
            EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
            if (relationInfo == null) {
                serviceResult.setError(new ResourceNotFoundErrorInfo(dto_name));
                return serviceResult;
            }
            Class serviceName = relationInfo.getService();
            IService s = (IService) applicationContext.getBean(serviceName);
            Object oriEntity = s.getById(id);
            if (oriEntity == null) {
//            result.okResult(null);
                return serviceResult;
            }
            Object dto = converter.convert(oriEntity, relationInfo.getDto());

            if (dto != null && !StringUtils.isEmpty(sub_entities)) {
                Map<String, Join[]> tableJoinerMap = Arrays.asList(sub_entities
                        .split(",")).stream()
                        .collect(Collectors.toMap(x -> x, y -> {
                            Join[] t = null;
                            try {
                                t = FieldUtils.getFieldAnnotationByName(dto, FieldUtils.unCap(y), Join.class);
                            } catch (NoSuchFieldException e) {
                                return null;
                            }
                            return t;
                        }));

                List<String> subEntityList = new ArrayList<>();
                for (String fieldName : tableJoinerMap.keySet()) {
                    Join[] joins = tableJoinerMap.get(fieldName);
                    if (joins == null || joins.length == 0) {
                        subEntityList.add(fieldName);
                    }
                }
                String keyColumn = TableInfoHelper.getTableInfo(relationInfo.getEntity()).getKeyColumn();
                SubFieldInfo.setSubDtoAfterQueryById(applicationContext, converter, entityDtoServiceRelationMap
                        , relationInfo, dto, subEntityList, keyColumn, id);

                SubFieldInfo.setSubDtoAfterQueryByTableJoiner(applicationContext, converter, entityDtoServiceRelationMap
                        , relationInfo, dto, tableJoinerMap, groupName);
            }
            serviceResult.setData(dto);
        } catch (Exception ex) {
            serviceResult.setError(new ExceptionErrorInfo(ex));
        }
        return serviceResult;
    }

    default IServiceResult getPagesByDtoAnnotation(
            String dto_name
            , Integer page_index
            , Integer page_size
            , String queryGroup
            , Map map) {
        IServiceResult serviceResult = new DefaultServiceResult();
        try {
            if(queryGroup == null) {
                queryGroup = "";
            }

            ApplicationContext applicationContext = getApplicationContext();
            EnhancedConversionService converter = getEnhancedConversionService();
            IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
            String entityName = FieldUtils.parseClassNameFromPathVariable(dto_name);
            EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
            if(relationInfo == null) {
                serviceResult.setError(new ResourceNotFoundErrorInfo(dto_name));
                return serviceResult;
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

        } catch (Exception ex) {
            serviceResult.setError(new ExceptionErrorInfo(ex));
        }
        return serviceResult;
    }

    default IServiceResult getPagesByQueryFields(
             String dto_name
            , Integer page_index
            , Integer page_size
            , List<QueryFieldModel> queryFiledModelList) {
        IServiceResult serviceResult = new DefaultServiceResult();
        try {
            ApplicationContext applicationContext = getApplicationContext();
            EnhancedConversionService converter = getEnhancedConversionService();
            IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
            String entityName = FieldUtils.parseClassNameFromPathVariable(dto_name);
            EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
            if(relationInfo == null) {
                serviceResult.setError(new ResourceNotFoundErrorInfo(dto_name));
                return serviceResult;
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

        } catch (Exception ex) {
            serviceResult.setError(new ExceptionErrorInfo(ex));
        }
        return serviceResult;
    }

    default IServiceResult deleteById(
             String dto_name
            , Serializable id
            , Boolean physic) {
        IServiceResult serviceResult = new DefaultServiceResult();
        try {
            ApplicationContext applicationContext = getApplicationContext();
            IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
            ICrudService crudService = getCrudService();
            String entityName = FieldUtils.parseClassNameFromPathVariable(dto_name);
            EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
            if(relationInfo == null) {
                serviceResult.setError(new ResourceNotFoundErrorInfo(dto_name));
                return serviceResult;
            }
            boolean deletePhysic = physic != null && physic == true;
            String keyColumn = TableInfoHelper.getTableInfo(relationInfo.getEntity()).getKeyColumn();

            boolean deleteResult = crudService.deleteById(relationInfo, keyColumn, id, deletePhysic, true);
            if(!deleteResult) {
                serviceResult.setError(new DeleteFailureErrorInfo());
            }
        } catch (Exception ex) {
            serviceResult.setError(new ExceptionErrorInfo(ex));
        }
        return serviceResult;
    }

    default IServiceResult deleteByIds(
             String dto_name
            , List<Serializable> ids
            , Boolean physic) {
        IServiceResult serviceResult = new DefaultServiceResult();
        try {
            ApplicationContext applicationContext = getApplicationContext();
            ICrudService crudService = getCrudService();
            String entityName = FieldUtils.parseClassNameFromPathVariable(dto_name);
            IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
            EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
            if(relationInfo == null) {
                serviceResult.setError(new ResourceNotFoundErrorInfo(dto_name));
                return serviceResult;
            }
            boolean deletePhysic = physic != null && physic == true;
            String keyColumn = TableInfoHelper.getTableInfo(relationInfo.getEntity()).getKeyColumn();

            boolean deleteResult = crudService.deleteByIds(relationInfo, keyColumn, ids, deletePhysic, true);
            if(!deleteResult) {
                serviceResult.setError(new DeleteFailureErrorInfo());
            }
        } catch (Exception ex) {
            serviceResult.setError(new ExceptionErrorInfo(ex));
        }
        return serviceResult;
    }

    default IServiceResult saveEntity(
             String dto_name
            , Map map) {
        IServiceResult serviceResult = new DefaultServiceResult();
        try {
            ApplicationContext applicationContext = getApplicationContext();
            Validator validator = getValidator();
            ICrudService crudService = getCrudService();
            IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
            String entityName = FieldUtils.parseClassNameFromPathVariable(dto_name);
            EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
            if(relationInfo == null) {
                serviceResult.setError(new ResourceNotFoundErrorInfo(dto_name));
                return serviceResult;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            Object o= objectMapper.convertValue(map, relationInfo.getDto());
            BindException errors = new BindException(o, dto_name);
            if(validator != null) {
                validator.validate(o, errors);
            }
            if(errors.hasErrors()) {
                serviceResult.setError(new FieldErrorInfo(errors.getMessage(), errors.getFieldErrors()));
                return serviceResult;
            }
            TableInfo tableInfo = TableInfoHelper.getTableInfo(relationInfo.getEntity());
            boolean saveResult = crudService.save(relationInfo, tableInfo, o);
            if(!saveResult) {
                serviceResult.setError(new InsertFailureErrorInfo());
            }

        } catch (Exception ex) {
            serviceResult.setError(new ExceptionErrorInfo(ex));
        }
        return serviceResult;
    }

    default IServiceResult updateEntity(
             String dto_name
            , Serializable id
            , Map map
            , String subEntities) {
        IServiceResult serviceResult = new DefaultServiceResult();
        try {
            ApplicationContext applicationContext = getApplicationContext();
            ICrudService crudService = getCrudService();
            Validator validator = getValidator();
            IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
            String entityName = FieldUtils.parseClassNameFromPathVariable(dto_name);
            EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
            if(relationInfo == null) {
                serviceResult.setError(new ResourceNotFoundErrorInfo(dto_name));
                return serviceResult;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            Object o= objectMapper.convertValue(map, relationInfo.getDto());

            BindException errors = new BindException(o, dto_name);
            if(validator != null) {
                validator.validate(o, errors);
            }
            if(errors.hasErrors()) {
                serviceResult.setError(new FieldErrorInfo(errors.getMessage(), errors.getFieldErrors()));
                return serviceResult;
            }

            CascadeUpdateStrategy updateStrategy = CascadeUpdateStrategy.NONE;
            List<String> updateEntityList = null;
            if(!StringUtils.isEmpty(subEntities)) {
                updateStrategy = CascadeUpdateStrategy.REMOVE_ASSIGN;
                updateEntityList = Arrays.stream(subEntities.split(",")).map(x -> x.trim()).collect(Collectors.toList());
            }

            TableInfo tableInfo = TableInfoHelper.getTableInfo(relationInfo.getEntity());
            boolean updateResult = crudService.update(relationInfo, tableInfo, id, o, updateStrategy, updateEntityList);
            if(!updateResult) {
                serviceResult.setError(new UpdateFailureErrorInfo());
            }
        } catch (Exception ex) {
            serviceResult.setError(new ExceptionErrorInfo(ex));
        }
        return serviceResult;
    }

    default IServiceResult businessUpdate(
             String dto_name
            , Map map) {
        IServiceResult serviceResult = new DefaultServiceResult();
        IUpdateObjectProvider provider = null;
        try {
            IEntityDtoServiceRelationMap entityDtoServiceRelationMap = getEntityDtoServiceRelationMap();
            ApplicationContext applicationContext = getApplicationContext();
            ICrudService crudService = getCrudService();
            Validator validator = getValidator();
            String entityName = FieldUtils.parseClassNameFromPathVariable(dto_name);
            EntityDtoServiceRelation relationInfo = entityDtoServiceRelationMap.getByDtoName(entityName);
            if(relationInfo == null) {
                serviceResult.setError(new ResourceNotFoundErrorInfo(dto_name));
                return serviceResult;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            Object o= objectMapper.convertValue(map, relationInfo.getDto());

            BindException errors = new BindException(o, dto_name);
            if(validator != null) {
                validator.validate(o, errors);
            }
            if(errors.hasErrors()) {
                serviceResult.setError(new FieldErrorInfo(errors.getMessage(), errors.getFieldErrors()));
                return serviceResult;
            }
            provider = applicationContext.getBean(relationInfo.getConverter());
            provider.validateBeforeUpdate(o, errors);
            if(errors.hasErrors()) {
                serviceResult.setError(new FieldErrorInfo(errors.getMessage(), errors.getFieldErrors()));
                return serviceResult;
            }

            List<UpdateEntity> entities = provider.createUpdateEntities(o);
            boolean updateResult = crudService.updateStatus(entities);
            if(!updateResult) {
                serviceResult.setError(new UpdateFailureErrorInfo());
                provider.onFail();
            } else {
                provider.onSuccess();
            }
            provider.onEnd();
        } catch (Exception ex) {
            serviceResult.setError(new ExceptionErrorInfo(ex));
            if(provider != null) {
                provider.onFail();
                provider.onEnd();
            }
        }
        return serviceResult;
    }
}
