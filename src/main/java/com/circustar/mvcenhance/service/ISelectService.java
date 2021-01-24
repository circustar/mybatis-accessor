package com.circustar.mvcenhance.service;

import com.circustar.mvcenhance.annotation.QueryFieldModel;
import com.circustar.mvcenhance.response.PageInfo;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;

import java.io.Serializable;
import java.util.List;

public interface ISelectService {
    Object getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children) throws Exception;

    Object getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children
            , String queryGroup) throws Exception;

    <T> PageInfo<T> getPagesByDtoAnnotation(EntityDtoServiceRelation relationInfo
            , Object object, String queryGroup
            , Integer page_index
            , Integer page_size
            ) throws Exception;

    <T> PageInfo<T> getPagesByQueryFields(EntityDtoServiceRelation relationInfo
            , List<QueryFieldModel> queryFiledModelList
            , Integer page_index
            , Integer page_size
            ) throws Exception;

    List getListByDtoAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , String queryGroup
    ) throws Exception;

    <T> List<T> getListByQueryFields(EntityDtoServiceRelation relationInfo
            , List<QueryFieldModel> queryFiledModelList
    ) throws Exception;

}
