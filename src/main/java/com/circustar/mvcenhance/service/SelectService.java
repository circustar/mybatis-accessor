package com.circustar.mvcenhance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.annotation.Selector;
import com.circustar.mvcenhance.annotation.QueryFieldModel;
import com.circustar.mvcenhance.response.PageInfo;
import com.circustar.mvcenhance.classInfo.DtoClassInfo;
import com.circustar.mvcenhance.classInfo.DtoClassInfoHelper;
import com.circustar.mvcenhance.classInfo.DtoFields;
import com.circustar.mvcenhance.mapper.MybatisPlusMapper;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.utils.AnnotationUtils;
import com.circustar.mvcenhance.utils.FieldUtils;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

public class SelectService implements ISelectService {
    public SelectService(ApplicationContext applicationContext
            , IEntityDtoServiceRelationMap entityDtoServiceRelationMap
            , DtoClassInfoHelper dtoClassInfoHelper) {
        this.applicationContext = applicationContext;
        this.entityDtoServiceRelationMap = entityDtoServiceRelationMap;
        this.dtoClassInfoHelper = dtoClassInfoHelper;
    }
    private ApplicationContext applicationContext;

    private IEntityDtoServiceRelationMap entityDtoServiceRelationMap;

    private DtoClassInfoHelper dtoClassInfoHelper;

    @Override
    public Object getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children) throws Exception {
        return getDtoById(relationInfo, id, children, "");
    }

    @Override
    public Object getDtoById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children
            , String queryGroup) throws Exception {
        IService s = applicationContext.getBean(relationInfo.getService());
        Object oriEntity = s.getById(id);
        if (oriEntity == null) {
            return null;
        }
        Object result = this.dtoClassInfoHelper.convertFromEntity(oriEntity, relationInfo.getDto());
        List<String> childList;
        if(children == null) {
//            childList = dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDto())
//                    .getSubDtoFieldList().stream().map(x -> x.getFieldName()).collect(Collectors.toList());
            childList = Collections.emptyList();
        } else {
            childList = Arrays.asList(children);
        }
        List<Field> subFields = FieldUtils.getExistFields(result, childList, false);

        Map<String, Selector[]> tableJoinerMap = new HashMap<>();
        List<String> noAnnotationInfoList = new ArrayList<>();
        AnnotationUtils.parseFieldAnnotationToMap(subFields, Selector.class
                , tableJoinerMap, noAnnotationInfoList);

        DtoFields.queryAndAssignDtoField(applicationContext, dtoClassInfoHelper, entityDtoServiceRelationMap
                , relationInfo, noAnnotationInfoList, result, id);

        DtoFields.queryAndAssignDtoField(applicationContext, dtoClassInfoHelper
                , entityDtoServiceRelationMap
                , relationInfo
                , result
                , tableJoinerMap
                , queryGroup);

        return result;
    }

    @Override
    public <T> PageInfo<T> getPagesByDtoAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , String queryGroup
            , Integer page_index
            , Integer page_size
            ) throws Exception {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDto());
        List<QueryFieldModel> queryFiledModelList = QueryFieldModel.getQueryFieldModeFromDto(dtoClassInfo, object, queryGroup);

        return getPagesByQueryFields(relationInfo, queryFiledModelList, page_index, page_size);
    }

    @Override
    public <T> PageInfo<T> getPagesByQueryFields(EntityDtoServiceRelation relationInfo
            , List<QueryFieldModel> queryFiledModelList
            , Integer page_index
            , Integer page_size
            ) throws Exception {
        IService service = applicationContext.getBean(relationInfo.getService());
        QueryWrapper qw = new QueryWrapper();
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDto());
        TableInfo tableInfo = TableInfoHelper.getTableInfo(dtoClassInfo.getEntityClassInfo().getClazz());
        QueryFieldModel.setQueryWrapper(tableInfo.getTableName()
                , queryFiledModelList, qw);

        PageInfo pageInfo = null;
        Page page = new Page(page_index, page_size);
        IPage pageResult = null;

        if (dtoClassInfo.containchild()) {
            pageResult = ((MybatisPlusMapper) service.getBaseMapper()).selectPageWithJoin(page, qw, dtoClassInfo.getJoinTables(), dtoClassInfo.getJointColumns());
        } else {
            pageResult = service.page(page, qw);
        }
        List<T> dtoList = (List) dtoClassInfoHelper.convertFromEntityList(pageResult.getRecords(), relationInfo.getDto());
        pageInfo = new PageInfo(pageResult.getTotal(), pageResult.getSize(), pageResult.getCurrent(), dtoList);

        return pageInfo;
    }

    @Override
    public List getListByDtoAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , String queryGroup
    ) throws Exception {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDto());
        List<QueryFieldModel> queryFiledModelList = QueryFieldModel.getQueryFieldModeFromDto(dtoClassInfo, object, queryGroup);

        return getListByQueryFields(relationInfo, queryFiledModelList);
    }

    @Override
    public <T> List<T> getListByQueryFields(EntityDtoServiceRelation relationInfo
            , List<QueryFieldModel> queryFiledModelList
    ) throws Exception {
        IService service = applicationContext.getBean(relationInfo.getService());
        List<T> dtoList = null;
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDto());
        TableInfo tableInfo = TableInfoHelper.getTableInfo(dtoClassInfo.getEntityClassInfo().getClazz());
        QueryWrapper qw = new QueryWrapper();
        QueryFieldModel.setQueryWrapper(tableInfo.getTableName()
                ,queryFiledModelList, qw);
        List entityList = null;
        if (dtoClassInfo.containchild()) {
            entityList = ((MybatisPlusMapper)service.getBaseMapper()).selectListWithJoin(qw, dtoClassInfo.getJoinTables(), dtoClassInfo.getJointColumns());
        } else {
            entityList = service.list(qw);
        }
        dtoList = (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityList, relationInfo.getDto());;

        return dtoList;
    }
}
