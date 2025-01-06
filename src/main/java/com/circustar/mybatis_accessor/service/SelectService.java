package com.circustar.mybatis_accessor.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.response.PageInfo;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.class_info.DtoFields;
import com.circustar.mybatis_accessor.mapper.CommonMapper;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.common_utils.parser.SPELParser;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class SelectService implements ISelectService {
    private final DtoClassInfoHelper dtoClassInfoHelper;
    public SelectService(DtoClassInfoHelper dtoClassInfoHelper) {
        this.dtoClassInfoHelper = dtoClassInfoHelper;
    }

    @Override
    public <T> T getEntityByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) {
        return this.getEntityByAnnotation(relationInfo, object, null);
    }

    @Override
    public <T> T getEntityByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, List<String> children
    ) {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        QueryWrapper queryWrapper = dtoClassInfo.createQueryWrapper(object);
        return getEntityByQueryWrapper(relationInfo, object, children, queryWrapper);
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
        return this.getEntityByQueryWrapper(relationInfo, dto, null, queryWrapper);
    }

    @Override
    public <T> T getEntityByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, List<String> children
            , QueryWrapper queryWrapper) {
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        IService service = dtoClassInfo.getServiceBean();
        String joinExpression = getJoinExpression(dtoClassInfo, dto, children);
        T result;
        if (!StringUtils.isEmpty(joinExpression)) {
            String joinColumns = dtoClassInfo.getJoinColumns(children);
            result = (T) ((CommonMapper)service.getBaseMapper()).selectOneWithJoin(queryWrapper
                    , joinExpression, (StringUtils.hasLength(joinColumns)?",": "") + joinColumns);
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
        return this.getEntityById(relationInfo, id , null);
    }

    @Override
    public <T> T getEntityById(EntityDtoServiceRelation relationInfo
            , Serializable id, List<String> children){
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        IService service = dtoClassInfo.getServiceBean();
        String joinExpression = getJoinExpression(dtoClassInfo, null, children);
        T result;
        if (!StringUtils.isEmpty(joinExpression)) {
            String joinColumns = dtoClassInfo.getJoinColumns(children);
            QueryWrapper qw = new QueryWrapper();
            qw.eq(dtoClassInfo.getEntityClassInfo().getTableInfo().getTableName()
                    + "." + dtoClassInfo.getEntityClassInfo().getTableInfo().getKeyColumn(), id);
            result = (T) ((CommonMapper)service.getBaseMapper()).selectOneWithJoin(qw
                    , joinExpression, (StringUtils.hasLength(joinColumns)?",": "") + joinColumns);
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
            , Integer pageIndex
            , Integer pageSize
            ) {
        return this.getEntityPageByAnnotation(relationInfo, object, null, pageIndex, pageSize);
    }

    @Override
    public <T> PageInfo<T> getEntityPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, List<String> joinNames
            , Integer pageIndex
            , Integer pageSize
    ) {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        QueryWrapper queryWrapper = dtoClassInfo.createQueryWrapper(object);

        return getEntityPageByQueryWrapper(relationInfo, object, joinNames, queryWrapper, pageIndex, pageSize);
    }

    @Override
    public <T> PageInfo<T> getDtoPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer pageIndex
            , Integer pageSize
    ) {
        return this.getDtoPageByAnnotation(relationInfo, object, null, pageIndex, pageSize);
    }

    @Override
    public <T> PageInfo<T> getDtoPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, List<String> joinNames
            , Integer pageIndex
            , Integer pageSize
    ) {
        PageInfo entityPage = getEntityPageByAnnotation(relationInfo, object, joinNames, pageIndex, pageSize);
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        List<T> dtoList = (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityPage.getRecords(), dtoClassInfo);
        return new PageInfo<>(entityPage.getTotal(), entityPage.getSize(), entityPage.getCurrent(), dtoList);
    }

    @Override
    public <T> PageInfo<T> getEntityPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto
            , QueryWrapper queryWrapper
            , Integer pageIndex
            , Integer pageSize
            ) {
        return this.getEntityPageByQueryWrapper(relationInfo, dto, null, queryWrapper, pageIndex, pageSize);
    }

    @Override
    public <T> PageInfo<T> getEntityPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, List<String> joinNames
            , QueryWrapper queryWrapper
            , Integer pageIndex
            , Integer pageSize
    ) {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        IService service = dtoClassInfo.getServiceBean();

        Page page = new Page(pageIndex, pageSize);
        IPage pageResult;

        String joinExpression = getJoinExpression(dtoClassInfo, dto, joinNames);
        if (!StringUtils.isEmpty(joinExpression)) {
            String joinColumns = dtoClassInfo.getJoinColumns(null);
            pageResult = ((CommonMapper) service.getBaseMapper()).selectPageWithJoin(page, queryWrapper
                    , joinExpression, (StringUtils.hasLength(joinColumns)?",": "") + joinColumns);
        } else {
            pageResult = service.page(page, queryWrapper);
        }
        return new PageInfo(pageResult.getTotal(), pageResult.getSize(), pageResult.getCurrent(), pageResult.getRecords());
    }

    @Override
    public <T> PageInfo<T> getDtoPageByQueryWrapper(EntityDtoServiceRelation relationInfo, Object object
            , QueryWrapper queryWrapper
            , Integer pageIndex
            , Integer pageSize
    ) {
        return this.getDtoPageByQueryWrapper(relationInfo, object, null, queryWrapper, pageIndex, pageSize);
    }

    @Override
    public <T> PageInfo<T> getDtoPageByQueryWrapper(EntityDtoServiceRelation relationInfo, Object object
            , List<String> joinNames
            , QueryWrapper queryWrapper
            , Integer pageIndex
            , Integer pageSize
    ) {
        PageInfo entityPage = getEntityPageByQueryWrapper(relationInfo, object, joinNames, queryWrapper, pageIndex, pageSize);
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        List<T> dtoList = (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityPage.getRecords(), dtoClassInfo);
        return new PageInfo<>(entityPage.getTotal(), entityPage.getSize(), entityPage.getCurrent(), dtoList);
    }

    @Override
    public <T> List<T> getEntityListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) {
        return this.getEntityListByAnnotation(relationInfo, object, null);
    }

    @Override
    public <T> List<T> getEntityListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, List<String> joinNames
    ) {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        QueryWrapper queryWrapper = dtoClassInfo.createQueryWrapper(object);

        return getEntityListByQueryWrapper(relationInfo, object, joinNames, queryWrapper);
    }

    @Override
    public <T> List<T> getDtoListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) {
        return this.getDtoListByAnnotation(relationInfo, object, null);
    }

    @Override
    public <T> List<T> getDtoListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, List<String> joinNames
    ) {
        List entityList = getEntityListByAnnotation(relationInfo, object, joinNames);
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        return (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityList, dtoClassInfo);
    }

    @Override
    public <T> List<T> getEntityListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, QueryWrapper queryWrapper
    ) {
        return this.getEntityListByQueryWrapper(relationInfo, dto, null, queryWrapper);
    }

    @Override
    public <T> List<T> getEntityListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, List<String> joinNames , QueryWrapper queryWrapper
    ) {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        IService service = dtoClassInfo.getServiceBean();
        List entityList;
        String joinExpression = getJoinExpression(dtoClassInfo, dto, joinNames);
        if (!StringUtils.isEmpty(joinExpression)) {
            String joinColumns = dtoClassInfo.getJoinColumns(null);
            entityList = ((CommonMapper)service.getBaseMapper()).selectListWithJoin(queryWrapper
                    , joinExpression, (StringUtils.hasLength(joinColumns)?",": "") + joinColumns);
        } else {
            entityList = service.list(queryWrapper);
        }

        return entityList;
    }


    @Override
    public <T> List<T> getDtoListByQueryWrapper(EntityDtoServiceRelation relationInfo, Object object
            , QueryWrapper queryWrapper
    ) {
        return this.getDtoListByQueryWrapper(relationInfo, object, null, queryWrapper);
    }

    @Override
    public <T> List<T> getDtoListByQueryWrapper(EntityDtoServiceRelation relationInfo, Object object
            , List<String> joinNames , QueryWrapper queryWrapper
    ) {
        List entityList = getEntityListByQueryWrapper(relationInfo, object, joinNames, queryWrapper);
        DtoClassInfo dtoClassInfo = dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        return (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityList, dtoClassInfo);
    }

    @Override
    public Long getCountByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ){
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        QueryWrapper queryWrapper = dtoClassInfo.createQueryWrapper(object);

        return this.getCountByQueryWrapper(relationInfo, object, queryWrapper);
    }

    @Override
    public Long getCountByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, QueryWrapper queryWrapper
    ) {
        return this.getCountByQueryWrapper(relationInfo, dto, null, queryWrapper);
    }

    @Override
    public Long getCountByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, List<String> joinNames, QueryWrapper queryWrapper
    ) {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo);
        IService service = dtoClassInfo.getServiceBean();
        Long result;
        String joinExpression = getJoinExpression(dtoClassInfo, dto, joinNames);
        if (!StringUtils.isEmpty(joinExpression)) {
            String joinColumns = dtoClassInfo.getJoinColumns(null);
            result = ((CommonMapper)service.getBaseMapper()).selectCountWithJoin(queryWrapper
                    , joinExpression, (StringUtils.hasLength(joinColumns)?",": "") + joinColumns);
        } else {
            result = service.count(queryWrapper);
        }

        return result;
    }

    private String getJoinExpression(DtoClassInfo dtoClassInfo, Object dto, List<String> joinNames) {
        return SPELParser.parseStringExpression(dto, dtoClassInfo.getJoinString(joinNames));
    }
}
