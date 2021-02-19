package com.circustar.mvcenhance.support;

import com.circustar.mvcenhance.annotation.SimpleWrapperPiece;
import com.circustar.mvcenhance.annotation.WrapperPiece;
import com.circustar.mvcenhance.error.ValidateException;
import com.circustar.mvcenhance.response.DefaultServiceResult;
import com.circustar.mvcenhance.response.IServiceResult;
import com.circustar.mvcenhance.response.PageInfo;
import com.circustar.mvcenhance.utils.MvcEnhanceConstants;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.provider.*;
import com.circustar.mvcenhance.utils.ArrayParamUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class ControllerSupport {
    private ServiceSupport serviceSupport;

    public ControllerSupport(ServiceSupport serviceSupport) {
        this.serviceSupport = serviceSupport;
    }

    /*
     *** 通过ID获取实体类，转换成dto后返回
     *** 指定sub_entities参数可返回关联的子项
     *** 检索子项时使用与groupName相匹配的EntityFilter的条件
     *** 子项中不存在EntityFilter注解时，默认使用主类ID作为检索条件
     */
    public IServiceResult getById(String dtoName
            , Serializable id
            , String sub_entities) throws Exception {

        IServiceResult serviceResult = new DefaultServiceResult();
        EntityDtoServiceRelation relationInfo = serviceSupport.parseEntityDtoServiceRelation(dtoName);
        Object data = serviceSupport.getById(relationInfo, id
                , ArrayParamUtils.convertStringToArray(sub_entities, ArrayParamUtils.DELIMITER_COMMA));
        serviceResult.setData(data);
        return serviceResult;
    }

    /*
     *** 读取dto中QueryField注解信息，组装成查询条件后查询实体列表，转化dto列表后返回
     *** page_index、page_size指定分页信息
     */
    public IServiceResult getPagesByDtoAnnotation(
            String dtoName
            , Integer page_index
            , Integer page_size
            , Map map) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();

        EntityDtoServiceRelation relationInfo = serviceSupport.parseEntityDtoServiceRelation(dtoName);
        Object dto = serviceSupport.convertFromMap(map, relationInfo.getDtoClass());

        if(page_index != null && page_size != null) {
            PageInfo pageInfo = serviceSupport.getPagesByAnnotation(relationInfo, dto
                    , page_index, page_size);

            serviceResult.setData(pageInfo);
        } else {
            List dataList = serviceSupport.getListByAnnotation(relationInfo, dto);
            serviceResult.setData(dataList);
        }

        return serviceResult;
    }

    /*
     *** QueryFieldModel设置查询条件后可查询实体列表，转化dto列表后返回
     *** page_index、page_size指定分页信息
     */
    public IServiceResult getPagesBySimpleQueryFields(
            String dtoName
            , Integer page_index
            , Integer page_size
            , List<SimpleWrapperPiece> simpleWrapperPieces) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();

        EntityDtoServiceRelation relationInfo = serviceSupport.parseEntityDtoServiceRelation(dtoName);
        List<WrapperPiece> queryFiledModelList = simpleWrapperPieces.stream()
                .map(x -> x.convertToWrapperPiece(relationInfo))
                .collect(Collectors.toList());
        if(page_index != null && page_size != null) {
            PageInfo pageInfo = serviceSupport.getPagesByWrapper(relationInfo
                    , queryFiledModelList, page_index, page_size);
            serviceResult.setData(pageInfo);
        } else {
            List dataList = serviceSupport.getListByWrapper(relationInfo, queryFiledModelList);
            serviceResult.setData(dataList);
        }

        return serviceResult;
    }

    public IServiceResult getPagesByQueryFields(
             String dtoName
            , Integer page_index
            , Integer page_size
            , List<WrapperPiece> queryFiledModelList) throws Exception {
        IServiceResult serviceResult = new DefaultServiceResult();

        EntityDtoServiceRelation relationInfo = serviceSupport.parseEntityDtoServiceRelation(dtoName);
        if(page_index != null && page_size != null) {
            PageInfo pageInfo = serviceSupport.getPagesByWrapper(relationInfo
                    , queryFiledModelList, page_index, page_size);
            serviceResult.setData(pageInfo);
        } else {
            List dataList = serviceSupport.getListByWrapper(relationInfo, queryFiledModelList);
            serviceResult.setData(dataList);
        }

        return serviceResult;
    }

    public IServiceResult saveEntity(String dtoName
            , Object updateObject
            , String children
            , boolean updateChildrenOnly) throws Exception {
        EntityDtoServiceRelation relationInfo = serviceSupport.parseEntityDtoServiceRelation(dtoName);
        Object dto = serviceSupport.convertFromMap(updateObject, relationInfo.getDtoClass());
        return saveEntity(dtoName, dto,  relationInfo, children, updateChildrenOnly);
    }

    public IServiceResult saveEntity(String dtoName
            , Object updateObject
            , EntityDtoServiceRelation relation
            , String children
            , boolean updateChildrenOnly) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        return updateDto(dtoName, updateObject, relation, DefaultInsertEntityProvider.getInstance(), options, true);
    }

    public IServiceResult saveEntities(String dtoName
            , List<Object> mapList
            , String children
            , boolean updateChildrenOnly
            , boolean returnUpdateResult) throws Exception {
        EntityDtoServiceRelation relationInfo = serviceSupport.parseEntityDtoServiceRelation(dtoName);
        List<Object> objects = serviceSupport.convertFromMapList(mapList, relationInfo.getDtoClass());
        return saveEntities(dtoName, objects, relationInfo, children, updateChildrenOnly, returnUpdateResult);
    }

    public IServiceResult saveEntities(String dtoName
            , List<Object> objects
            , EntityDtoServiceRelation relation
            , String children
            , boolean updateChildrenOnly
            , boolean returnUpdateResult) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);

        return updateDto(dtoName, objects, relation, DefaultInsertEntityProvider.getInstance()
                , options, returnUpdateResult);
    }

    public IServiceResult updateEntity(String dtoName
            , Object updateObject
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete) throws Exception {
        EntityDtoServiceRelation relationInfo = serviceSupport.parseEntityDtoServiceRelation(dtoName);
        Object dto = serviceSupport.convertFromMap(updateObject, relationInfo.getDtoClass());

        return updateEntity(dtoName, dto, relationInfo
                , children, updateChildrenOnly, removeAndInsertNewChild, physicDelete);
    }

    public IServiceResult updateEntity(String dtoName
            , Object updateObject
            , EntityDtoServiceRelation relation
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_AND_INSERT, removeAndInsertNewChild);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);

        return updateDto(dtoName, updateObject, relation, DefaultUpdateEntityProvider.getInstance(), options, true);
    }

    public IServiceResult updateEntities(String dtoName
            , List<Object> mapList
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete
            , boolean returnUpdateResult) throws Exception {
        EntityDtoServiceRelation relationInfo = serviceSupport.parseEntityDtoServiceRelation(dtoName);
        List<Object> objects = serviceSupport.convertFromMapList(mapList, relationInfo.getDtoClass());
        return updateEntities(dtoName, objects, relationInfo, children
                , updateChildrenOnly, removeAndInsertNewChild, physicDelete, returnUpdateResult);
    }

    public IServiceResult updateEntities(String dtoName
            , List<Object> objects
            , EntityDtoServiceRelation relation
            , String children
            , boolean updateChildrenOnly
            , boolean removeAndInsertNewChild
            , boolean physicDelete
            , boolean returnUpdateResult) throws Exception {
        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, ArrayParamUtils.convertStringToArray(children));
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_DELETE_AND_INSERT, removeAndInsertNewChild);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);

        return updateDto(dtoName, objects, relation, DefaultUpdateEntityProvider.getInstance()
                , options, returnUpdateResult);
    }

    public IServiceResult deleteById(String dtoName, String children
            , Serializable id
            , boolean updateChildrenOnly
            , boolean physicDelete) throws Exception  {
        return deleteByIds(dtoName, ArrayParamUtils.convertStringToArray(children), Collections.singleton(id)
                , updateChildrenOnly, physicDelete);
    }

    public IServiceResult deleteByIds(String dtoName
            , String[] children
            , Set<Serializable> ids
            , boolean updateChildrenOnly
            , boolean physicDelete) throws Exception  {
        EntityDtoServiceRelation relationInfo = serviceSupport.parseEntityDtoServiceRelation(dtoName);

        Map options = new HashMap();
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_TARGET_LIST, children);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_PHYSIC_DELETE, physicDelete);
        options.put(MvcEnhanceConstants.UPDATE_STRATEGY_UPDATE_CHILDREN_ONLY, updateChildrenOnly);

        return updateDto(dtoName, ids, relationInfo, DefaultDeleteEntityProvider.getInstance()
                , options, false);
    }

    public IServiceResult updateDto(
            String dtoName, Object dtoObject
            , String updateEntityProviderName, Map options, boolean returnUpdateResult) throws Exception {
        EntityDtoServiceRelation relationInfo = serviceSupport.parseEntityDtoServiceRelation(dtoName);
        IUpdateEntityProvider updateEntityProvider = serviceSupport.parseProviderByName(updateEntityProviderName);
        return updateDto(dtoName, relationInfo, updateEntityProvider, options, returnUpdateResult);
    }

    public IServiceResult updateDto(
            String dtoName, Object dtoObject
            , IUpdateEntityProvider updateEntityProvider, Map options, boolean returnUpdateResult) throws Exception {
        EntityDtoServiceRelation relationInfo = serviceSupport.parseEntityDtoServiceRelation(dtoName);
        return updateDto(dtoName, relationInfo, updateEntityProvider, options, returnUpdateResult);
    }

    public IServiceResult updateDto(
            String dtoName, Object dtoObject, EntityDtoServiceRelation relationInfo
            , IUpdateEntityProvider updateEntityProvider, Map options, boolean returnUpdateResult) throws Exception {

        IServiceResult serviceResult = new DefaultServiceResult();
        try {
            Collection<Object> updatedEntities = serviceSupport.updateObject(dtoName, dtoObject
                    , relationInfo, updateEntityProvider, options);
            if(returnUpdateResult) {
                serviceResult.setData(updatedEntities);
            }
        } catch (ValidateException ex) {
            serviceResult.setData(null);
            serviceResult.setGlobalErrorList(ex.getBindingResult().getGlobalErrors());
            serviceResult.setFieldErrorList(ex.getBindingResult().getFieldErrors());
        } catch (Exception ex) {
            serviceResult.setData(null);
            throw ex;
        } finally {
        }
        return serviceResult;
    }
}
