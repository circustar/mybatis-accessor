package com.circustar.mvcenhance.service;

import com.circustar.mvcenhance.annotation.WrapperPiece;
import com.circustar.mvcenhance.response.PageInfo;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;

import java.io.Serializable;
import java.util.List;

public interface ISelectService {
    Object getById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children) throws Exception;

    <T> PageInfo<T> getPagesByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
            ) throws Exception;

    <T> PageInfo<T> getPagesByWrapper(EntityDtoServiceRelation relationInfo
            , List<WrapperPiece> queryFiledModelList
            , Integer page_index
            , Integer page_size
            ) throws Exception;

    List getListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) throws Exception;

    <T> List<T> getListByWrapper(EntityDtoServiceRelation relationInfo
            , List<WrapperPiece> queryFiledModelList
    ) throws Exception;

}
