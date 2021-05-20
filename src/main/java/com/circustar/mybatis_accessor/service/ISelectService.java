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

    <T> T getDtoByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, String[] children
    );

    <T> T getEntityByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper);

    <T> T getDtoByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper, String[] children);

    <T> T getEntityById(EntityDtoServiceRelation relationInfo
            , Serializable id);

    <T> T getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children);

    <T> PageInfo<T> getEntityPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
    );

    <T> PageInfo<T> getDtoPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
            );

    <T> PageInfo<T> getEntityPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
            , Object dto
            , Integer page_index
            , Integer page_size
            );


    <T> PageInfo<T> getDtoPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    );

    <T> List<T> getEntityListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    );


    <T> List<T> getDtoListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    );

    <T> List<T> getEntityListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper, Object dto
    );

    <T> List<T> getDtoListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
    );
}
