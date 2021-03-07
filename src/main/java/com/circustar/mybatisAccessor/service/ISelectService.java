package com.circustar.mybatisAccessor.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mybatisAccessor.response.PageInfo;
import com.circustar.mybatisAccessor.relation.EntityDtoServiceRelation;

import java.io.Serializable;
import java.util.List;

public interface ISelectService {
    <T> T getEntityByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) throws Exception;

    <T> T getDtoByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, String[] children
    ) throws Exception;

    <T> T getEntityByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper) throws Exception;

    <T> T getDtoByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper, String[] children) throws Exception;

    <T> T getEntityById(EntityDtoServiceRelation relationInfo
            , Serializable id) throws Exception;

    <T> T getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children) throws Exception;

    <T> PageInfo<T> getEntityPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
    ) throws Exception;

    <T> PageInfo<T> getDtoPageByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
            ) throws Exception;

    <T> PageInfo<T> getEntityPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
            ) throws Exception;


    <T> PageInfo<T> getDtoPageByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
            , Integer page_index
            , Integer page_size
    ) throws Exception;

    <T> List<T> getEntityListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) throws Exception;


    <T> List<T> getDtoListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) throws Exception;

    <T> List<T> getEntityListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
    )  throws Exception;

    <T> List<T> getDtoListByQueryWrapper(EntityDtoServiceRelation relationInfo
            , QueryWrapper queryWrapper
    )  throws Exception;
}
