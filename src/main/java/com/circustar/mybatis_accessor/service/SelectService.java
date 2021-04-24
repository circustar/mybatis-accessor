package com.circustar.mybatis_accessor.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.response.PageInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoFields;
import com.circustar.mybatis_accessor.mapper.MybatisPlusMapper;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.common_utils.parser.SPELParser;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class SelectService implements ISelectService {
    public SelectService(ApplicationContext applicationContext
            , IEntityDtoServiceRelationMap entityDtoServiceRelationMap
            , DtoClassInfoHelper dtoClassInfoHelper) {
        this.applicationContext = applicationContext;
        this.entityDtoServiceRelationMap = entityDtoServiceRelationMap;
        this.dtoClassInfoHelper = dtoClassInfoHelper;
    }
    private ApplicationContext applicationContext;

    private IEntityDtoServiceRelationMap entityDtoServiceRelationMap;

    private DtoClassInfoHelper dtoClassInfoHelper;

    @Override
    public <T> T getEntityByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) throws Exception {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        QueryWrapper queryWrapper = dtoClassInfo.createQueryWrapper(this.dtoClassInfoHelper, object);
        return getEntityByQueryWrapper(relationInfo, queryWrapper);
    }

    @Override
    public <T> T getDtoByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, String[] children
    ) throws Exception {
        Object oriEntity = this.getEntityByAnnotation(relationInfo, object);
        if (oriEntity == null) {
            return null;
        }
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        T result = (T) this.dtoClassInfoHelper.convertFromEntity(oriEntity, relationInfo.getDtoClass());
        if(dtoClassInfo.getEntityClassInfo().getKeyField() != null) {
            Serializable id = (Serializable) FieldUtils.getFieldValue(oriEntity, dtoClassInfo.getEntityClassInfo().getKeyField().getReadMethod());
            if(id != null) {
                setDtoChildren(relationInfo, result, id, children);
            }
        }
        return result;
    }

    @Override
    public <T> T getEntityByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper) throws Exception {
        IService s = relationInfo.getServiceBean(applicationContext);
        return (T)s.getOne(queryWrapper);
    }

    @Override
    public <T> T getDtoByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
            , String[] children) throws Exception {
        IService s = relationInfo.getServiceBean(applicationContext);
        Object oriEntity = s.getOne(queryWrapper);
        if (oriEntity == null) {
            return null;
        }
        T result = (T) this.dtoClassInfoHelper.convertFromEntity(oriEntity, relationInfo.getDtoClass());
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        if(dtoClassInfo.getEntityClassInfo().getKeyField() != null) {
            Serializable id = (Serializable) FieldUtils.getFieldValue(oriEntity, dtoClassInfo.getEntityClassInfo().getKeyField().getReadMethod());
            if(id != null) {
                setDtoChildren(relationInfo, result, id, children);
            }
        }
        return result;
    }

    private void setDtoChildren(EntityDtoServiceRelation relationInfo
            , Object dto
            , Serializable id
            , String[] children) throws Exception {
        Set<String> childList;
        if(children == null) {
            childList = Collections.emptySet();
        } else {
            childList = new HashSet<>(Arrays.asList(children));
        }
        List<DtoField> subFields = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass())
                .getSubDtoFieldList().stream().filter(x -> childList.contains(x.getField().getName())).collect(Collectors.toList());

        Map<Boolean, List<DtoField>> dtoFieldMap = subFields.stream()
                .collect(Collectors.partitioningBy(x -> x.getSelectors() == null || x.getSelectors().length == 0));

        List<DtoField> fieldsWithNoSelector = dtoFieldMap.get(true);
        if(fieldsWithNoSelector != null && fieldsWithNoSelector.size() > 0) {
            DtoFields.queryAndAssignDtoFieldById(applicationContext, dtoClassInfoHelper, entityDtoServiceRelationMap
                    , relationInfo, fieldsWithNoSelector, dto, id);
        }

        List<DtoField> fieldsWithSelector = dtoFieldMap.get(false);
        if(fieldsWithSelector != null && fieldsWithSelector.size() > 0) {
            DtoFields.queryAndAssignDtoFieldBySelector(applicationContext, dtoClassInfoHelper
                    , entityDtoServiceRelationMap
                    , relationInfo
                    , dto
                    , fieldsWithSelector);
        }
    }

    @Override
    public <T> T getEntityById(EntityDtoServiceRelation relationInfo
            , Serializable id) throws Exception {
        IService s = relationInfo.getServiceBean(applicationContext);
        return (T)s.getById(id);
    }

    @Override
    public <T> T getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children) throws Exception {
        IService s = relationInfo.getServiceBean(applicationContext);
        Object oriEntity = s.getById(id);
        if (oriEntity == null) {
            return null;
        }
        T result = (T) this.dtoClassInfoHelper.convertFromEntity(oriEntity, relationInfo.getDtoClass());
        setDtoChildren(relationInfo, result, id , children);

        return result;
    }

    @Override
    public <T> PageInfo<T> getEntityPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
            )  throws Exception {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        QueryWrapper queryWrapper = dtoClassInfo.createQueryWrapper(this.dtoClassInfoHelper, object);

        return getEntityPageByQueryWrapper(relationInfo, queryWrapper, object, page_index, page_size);
    }

    @Override
    public <T> PageInfo<T> getDtoPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
    )  throws Exception {
        PageInfo entityPage = getEntityPageByAnnotation(relationInfo, object, page_index, page_size);
        List<T> dtoList = (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityPage.getRecords(), relationInfo.getDtoClass());;
        return new PageInfo<>(entityPage.getTotal(), entityPage.getSize(), entityPage.getCurrent(), dtoList);    }

    @Override
    public <T> PageInfo<T> getEntityPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
            , Object dto
            , Integer page_index
            , Integer page_size
            ) {
        IService service = relationInfo.getServiceBean(applicationContext);
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());

        Page page = new Page(page_index, page_size);
        IPage pageResult = null;

        String joinExpression = dtoClassInfo.getJoinTables();
        if(dto != null && !StringUtils.isEmpty(joinExpression)) {
            joinExpression = SPELParser.parseExpression(dto, joinExpression).toString();
        }
        if (!StringUtils.isEmpty(joinExpression)) {
            pageResult = ((MybatisPlusMapper) service.getBaseMapper()).selectPageWithJoin(page, queryWrapper
                    , joinExpression, dtoClassInfo.getJoinColumns());
        } else {
            pageResult = service.page(page, queryWrapper);
        }
        return new PageInfo(pageResult.getTotal(), pageResult.getSize(), pageResult.getCurrent(), pageResult.getRecords());
    }

    @Override
    public <T> PageInfo<T> getDtoPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    ) throws Exception {
        PageInfo entityPage = getEntityPageByQueryWrapper(relationInfo, queryWrapper, null, page_index, page_size);
        List<T> dtoList = (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityPage.getRecords(), relationInfo.getDtoClass());;
        return new PageInfo<>(entityPage.getTotal(), entityPage.getSize(), entityPage.getCurrent(), dtoList);
    }

    @Override
    public <T> List<T> getEntityListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    )  throws Exception {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        QueryWrapper queryWrapper = dtoClassInfo.createQueryWrapper(this.dtoClassInfoHelper, object);

        return getEntityListByQueryWrapper(relationInfo, queryWrapper, object);
    }

    @Override
    public <T> List<T> getDtoListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    )  throws Exception {
        List entityList = getEntityListByAnnotation(relationInfo, object);
        List<T> dtoList = (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityList, relationInfo.getDtoClass());;
        return dtoList;
    }

    @Override
    public <T> List<T> getEntityListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper, Object dto
    ) {
        IService service = relationInfo.getServiceBean(applicationContext);
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        List entityList = null;
        String joinExpression = dtoClassInfo.getJoinTables();
        if(dto != null && !StringUtils.isEmpty(joinExpression)) {
            joinExpression = SPELParser.parseExpression(dto, joinExpression).toString();
        }
        if (!StringUtils.isEmpty(joinExpression)) {
            entityList = ((MybatisPlusMapper)service.getBaseMapper()).selectListWithJoin(queryWrapper
                    , joinExpression, dtoClassInfo.getJoinColumns());
        } else {
            entityList = service.list(queryWrapper);
        }

        return entityList;
    }


    @Override
    public <T> List<T> getDtoListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
    )  throws Exception {
        List entityList = getEntityListByQueryWrapper(relationInfo, queryWrapper, null);
        List<T> dtoList = (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityList, relationInfo.getDtoClass());;
        return dtoList;
    }
}
