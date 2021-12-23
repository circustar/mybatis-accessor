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
import com.circustar.mybatis_accessor.mapper.CommonMapper;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.common_utils.parser.SPELParser;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class SelectService implements ISelectService {
    public SelectService(DtoClassInfoHelper dtoClassInfoHelper) {
        this.dtoClassInfoHelper = dtoClassInfoHelper;
    }
    private DtoClassInfoHelper dtoClassInfoHelper;

    @Override
    public <T> T getEntityByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        QueryWrapper queryWrapper = dtoClassInfo.createQueryWrapper(object);
        return getEntityByQueryWrapper(relationInfo, object, queryWrapper);
    }

    @Override
    public <T> T getDtoByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, boolean includeAllChildren, List<String> children
    ){
        Object oriEntity = this.getEntityByAnnotation(relationInfo, object);
        if (oriEntity == null) {
            return null;
        }
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        List<DtoField> subFields = DtoClassInfo.getDtoFieldsByName(dtoClassInfo, includeAllChildren, false, children);
        T result = (T) this.dtoClassInfoHelper.convertFromEntity(oriEntity, dtoClassInfo);
        if(dtoClassInfo.getEntityClassInfo().getKeyField() != null) {
            Serializable id = (Serializable) FieldUtils.getFieldValue(oriEntity
                    , dtoClassInfo.getEntityClassInfo().getKeyField().getPropertyDescriptor().getReadMethod());
            if (id != null) {
                setDtoChildren(relationInfo, dtoClassInfo, result, id, subFields);
            }
        }
        return result;
    }

    @Override
    public <T> T getEntityByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto
            , QueryWrapper queryWrapper) {
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        IService service = dtoClassInfo.getServiceBean();
        String joinExpression = getJoinExpression(dtoClassInfo, dto);
        T result;
        if (!StringUtils.isEmpty(joinExpression)) {
            result = (T) ((CommonMapper)service.getBaseMapper()).selectOneWithJoin(queryWrapper
                    , joinExpression, dtoClassInfo.getJoinColumns());
        } else {
            result = (T) service.getOne(queryWrapper);
        }

        return result;
    }

    @Override
    public <T> T getDtoByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto
            , QueryWrapper queryWrapper
            , boolean includeAllChildren
            , List<String> children) {
        Object oriEntity = getEntityByQueryWrapper(relationInfo, dto, queryWrapper);
        if (oriEntity == null) {
            return null;
        }
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        List<DtoField> subFields = DtoClassInfo.getDtoFieldsByName(dtoClassInfo, includeAllChildren, false, children);
        T result = (T) this.dtoClassInfoHelper.convertFromEntity(oriEntity, dtoClassInfo);
        if(dtoClassInfo.getEntityClassInfo().getKeyField() != null) {
            Serializable id = (Serializable) FieldUtils.getFieldValue(oriEntity
                    , dtoClassInfo.getEntityClassInfo().getKeyField().getPropertyDescriptor().getReadMethod());
            if(id != null) {
                setDtoChildren(relationInfo, dtoClassInfo, result, id, subFields);
            }
        }
        return result;
    }

    private void setDtoChildren(EntityDtoServiceRelation relationInfo
            , DtoClassInfo dtoClassInfo
            , Object dto
            , Serializable id
            , List<DtoField> subFields) {

        Map<Boolean, List<DtoField>> dtoFieldMap = subFields.stream().filter(x -> x.getQueryJoin() == null)
                .collect(Collectors.partitioningBy(x -> x.getSelectors() == null || x.getSelectors().size() == 0));

        List<DtoField> fieldsWithNoSelector = dtoFieldMap.get(true);
        if(fieldsWithNoSelector != null && !fieldsWithNoSelector.isEmpty()) {
            DtoFields.queryAndAssignDtoFieldById(dtoClassInfo
                    , relationInfo, fieldsWithNoSelector, dto, id);
        }

        List<DtoField> fieldsWithSelector = dtoFieldMap.get(false);
        if(fieldsWithSelector != null && !fieldsWithSelector.isEmpty()) {
            DtoFields.queryAndAssignDtoFieldBySelector(dtoClassInfo.getDtoClassInfoHelper()
                    , relationInfo
                    , dto
                    , fieldsWithSelector);
        }
    }

    @Override
    public <T> T getEntityById(EntityDtoServiceRelation relationInfo
            , Serializable id){
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        IService service = dtoClassInfo.getServiceBean();
        String joinExpression = getJoinExpression(dtoClassInfo, null);
        T result;
        if (!StringUtils.isEmpty(joinExpression)) {
            QueryWrapper qw = new QueryWrapper();
            qw.eq(dtoClassInfo.getEntityClassInfo().getTableInfo().getTableName()
                    + "." + dtoClassInfo.getEntityClassInfo().getTableInfo().getKeyColumn(), id);
            result = (T) ((CommonMapper)service.getBaseMapper()).selectOneWithJoin(qw
                    , joinExpression, dtoClassInfo.getJoinColumns());
        } else {
            result = (T) service.getById(id);
        }

        return result;
    }

    @Override
    public <T> T getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , boolean includeAllChildren
            , List<String> children) {
        Object oriEntity = getEntityById(relationInfo, id);
        if (oriEntity == null) {
            return null;
        }
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        List<DtoField> subFields = DtoClassInfo.getDtoFieldsByName(dtoClassInfo, includeAllChildren, false, children);
        T result = (T) this.dtoClassInfoHelper.convertFromEntity(oriEntity, dtoClassInfo);
        setDtoChildren(relationInfo, dtoClassInfo, result, id , subFields);

        return result;
    }

    @Override
    public <T> PageInfo<T> getEntityPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
            ) {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        QueryWrapper queryWrapper = dtoClassInfo.createQueryWrapper(object);

        return getEntityPageByQueryWrapper(relationInfo, object, queryWrapper, page_index, page_size);
    }

    @Override
    public <T> PageInfo<T> getDtoPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
    ) {
        PageInfo entityPage = getEntityPageByAnnotation(relationInfo, object, page_index, page_size);
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        List<T> dtoList = (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityPage.getRecords(), dtoClassInfo);;
        return new PageInfo<>(entityPage.getTotal(), entityPage.getSize(), entityPage.getCurrent(), dtoList);    }

    @Override
    public <T> PageInfo<T> getEntityPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
            ) {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        IService service = dtoClassInfo.getServiceBean();

        Page page = new Page(page_index, page_size);
        IPage pageResult = null;

        String joinExpression = getJoinExpression(dtoClassInfo, dto);
        if (!StringUtils.isEmpty(joinExpression)) {
            pageResult = ((CommonMapper) service.getBaseMapper()).selectPageWithJoin(page, queryWrapper
                    , joinExpression, dtoClassInfo.getJoinColumns());
        } else {
            pageResult = service.page(page, queryWrapper);
        }
        return new PageInfo(pageResult.getTotal(), pageResult.getSize(), pageResult.getCurrent(), pageResult.getRecords());
    }

    @Override
    public <T> PageInfo<T> getDtoPageByQueryWrapper(EntityDtoServiceRelation relationInfo, Object object
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    ) {
        PageInfo entityPage = getEntityPageByQueryWrapper(relationInfo, object, queryWrapper, page_index, page_size);
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        List<T> dtoList = (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityPage.getRecords(), dtoClassInfo);;
        return new PageInfo<>(entityPage.getTotal(), entityPage.getSize(), entityPage.getCurrent(), dtoList);
    }

    @Override
    public <T> List<T> getEntityListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        QueryWrapper queryWrapper = dtoClassInfo.createQueryWrapper(object);

        return getEntityListByQueryWrapper(relationInfo, object, queryWrapper);
    }

    @Override
    public <T> List<T> getDtoListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) {
        List entityList = getEntityListByAnnotation(relationInfo, object);
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        List<T> dtoList = (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityList, dtoClassInfo);;
        return dtoList;
    }

    @Override
    public <T> List<T> getEntityListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, QueryWrapper queryWrapper
    ) {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        IService service = dtoClassInfo.getServiceBean();
        List entityList = null;
        String joinExpression = getJoinExpression(dtoClassInfo, dto);
        if (!StringUtils.isEmpty(joinExpression)) {
            entityList = ((CommonMapper)service.getBaseMapper()).selectListWithJoin(queryWrapper
                    , joinExpression, dtoClassInfo.getJoinColumns());
        } else {
            entityList = service.list(queryWrapper);
        }

        return entityList;
    }


    @Override
    public <T> List<T> getDtoListByQueryWrapper(EntityDtoServiceRelation relationInfo, Object object
            , QueryWrapper queryWrapper
    ) {
        List entityList = getEntityListByQueryWrapper(relationInfo, object, queryWrapper);
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        List<T> dtoList = (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityList, dtoClassInfo);;
        return dtoList;
    }

    @Override
    public Integer getCountByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ){
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        QueryWrapper queryWrapper = dtoClassInfo.createQueryWrapper(object);

        return this.getCountByQueryWrapper(relationInfo, object, queryWrapper);
    }

    @Override
    public Integer getCountByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, QueryWrapper queryWrapper
    ) {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        IService service = dtoClassInfo.getServiceBean();
        Integer result;
        String joinExpression = getJoinExpression(dtoClassInfo, dto);
        if (!StringUtils.isEmpty(joinExpression)) {
            result = ((CommonMapper)service.getBaseMapper()).selectCountWithJoin(queryWrapper
                    , joinExpression, dtoClassInfo.getJoinColumns());
        } else {
            result = service.count(queryWrapper);
        }

        return result;
    }

    private String getJoinExpression(DtoClassInfo dtoClassInfo, Object dto) {
        String joinExpression = dtoClassInfo.getJoinTables();
        if(dto != null && !StringUtils.isEmpty(joinExpression)) {
            joinExpression = SPELParser.parseExpression(dto, joinExpression).toString();
        }
        return joinExpression;
    }
}
