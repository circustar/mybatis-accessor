package com.circustar.mvcenhance.service;

import com.circustar.mvcenhance.annotation.WrapperPiece;
import com.circustar.mvcenhance.response.PageInfo;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;

import java.io.Serializable;
import java.util.List;

public interface ISelectService {
    Object getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children) throws Exception;

    <T> PageInfo<T> getPagesByDtoAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
            ) throws Exception;

    <T> PageInfo<T> getPagesByQueryFields(EntityDtoServiceRelation relationInfo
            , List<WrapperPiece> queryFiledModelList
            , Integer page_index
            , Integer page_size
            ) throws Exception;

    List getListByDtoAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) throws Exception;

    <T> List<T> getListByQueryFields(EntityDtoServiceRelation relationInfo
            , List<WrapperPiece> queryFiledModelList
    ) throws Exception;

}
