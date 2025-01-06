package com.circustar.mybatis_accessor.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mybatis_accessor.response.PageInfo;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelation;

import java.io.Serializable;
import java.util.List;

public interface ISelectService {
    <T> T getEntityByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    );

    <T> T getEntityByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, List<String> children
    );


    <T> T getDtoByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, boolean includeAllChildren, List<String> children
    );

    <T> T getEntityByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, QueryWrapper queryWrapper);

    <T> T getEntityByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, List<String> children, QueryWrapper queryWrapper);

    <T> T getDtoByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, QueryWrapper queryWrapper, boolean includeAllChildren, List<String> children);

    <T> T getEntityById(EntityDtoServiceRelation relationInfo
            , Serializable id);

    <T> T getEntityById(EntityDtoServiceRelation relationInfo
            , Serializable id, List<String> children);

    <T> T getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , boolean includeAllChildren
            , List<String> children);

    <T> PageInfo<T> getEntityPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer pageIndex
            , Integer pageSize
    );

    <T> PageInfo<T> getEntityPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, List<String> joinNames
            , Integer pageIndex
            , Integer pageSize
    );

    <T> PageInfo<T> getDtoPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer pageIndex
            , Integer pageSize
            );

    <T> PageInfo<T> getDtoPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, List<String> joinNames
            , Integer pageIndex
            , Integer pageSize
    );

    <T> PageInfo<T> getEntityPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto
            , QueryWrapper queryWrapper
            , Integer pageIndex
            , Integer pageSize
            );

    <T> PageInfo<T> getEntityPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, List<String> joinNames
            , QueryWrapper queryWrapper
            , Integer pageIndex
            , Integer pageSize
    );

    <T> PageInfo<T> getDtoPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto
            , QueryWrapper queryWrapper
            , Integer pageIndex
            , Integer pageSize
    );

    <T> PageInfo<T> getDtoPageByQueryWrapper(EntityDtoServiceRelation relationInfo, Object object
            , List<String> joinNames
            , QueryWrapper queryWrapper
            , Integer pageIndex
            , Integer pageSize
    );

    <T> List<T> getEntityListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    );

    <T> List<T> getEntityListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, List<String> joinNames
    );

    <T> List<T> getDtoListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    );

    <T> List<T> getDtoListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, List<String> joinNames
    );

    <T> List<T> getEntityListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto
            , QueryWrapper queryWrapper
    );

    <T> List<T> getEntityListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, List<String> joinNames , QueryWrapper queryWrapper
    );

    <T> List<T> getDtoListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto
            , QueryWrapper queryWrapper
    );
    <T> List<T> getDtoListByQueryWrapper(EntityDtoServiceRelation relationInfo, Object object
            , List<String> joinNames , QueryWrapper queryWrapper
    );

    Long getCountByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    );


    Long getCountByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto
            , QueryWrapper queryWrapper
    );

    Long getCountByQueryWrapper(EntityDtoServiceRelation relationInfo
            , Object dto, List<String> joinNames, QueryWrapper queryWrapper
    );
}
