package com.circustar.mvcenhance.enhance.service;

import com.circustar.mvcenhance.common.query.QueryFieldModel;
import com.circustar.mvcenhance.common.response.PageInfo;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;

import java.io.Serializable;
import java.util.List;

public interface ISelectService {
    Object getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] subEntities) throws Exception;

    Object getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] subEntities
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
