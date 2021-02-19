package com.circustar.mvcenhance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.annotation.Selector;
import com.circustar.mvcenhance.annotation.WrapperPiece;
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
        IService s = relationInfo.getServiceBean(applicationContext);
        Object oriEntity = s.getById(id);
        if (oriEntity == null) {
            return null;
        }
        Object result = this.dtoClassInfoHelper.convertFromEntity(oriEntity, relationInfo.getDtoClass());
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
                , tableJoinerMap);

        return result;
    }

    @Override
    public <T> PageInfo<T> getPagesByDtoAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
            ) throws Exception {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        List<WrapperPiece> queryWrapper = WrapperPiece.getQueryWrapperFromDto(dtoClassInfo, object);

        return getPagesByQueryFields(relationInfo, queryWrapper, page_index, page_size);
    }

    @Override
    public <T> PageInfo<T> getPagesByQueryFields(EntityDtoServiceRelation relationInfo
            , List<WrapperPiece> queryFiledModelList
            , Integer page_index
            , Integer page_size
            ) {
        IService service = relationInfo.getServiceBean(applicationContext);
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        QueryWrapper qw = WrapperPiece.createQueryWrapper(queryFiledModelList);

        PageInfo pageInfo = null;
        Page page = new Page(page_index, page_size);
        IPage pageResult = null;

        if (dtoClassInfo.containchild()) {
            pageResult = ((MybatisPlusMapper) service.getBaseMapper()).selectPageWithJoin(page, qw, dtoClassInfo.getJoinTables(), dtoClassInfo.getJointColumns());
        } else {
            pageResult = service.page(page, qw);
        }
        List<T> dtoList = (List) dtoClassInfoHelper.convertFromEntityList(pageResult.getRecords(), relationInfo.getDtoClass());
        pageInfo = new PageInfo(pageResult.getTotal(), pageResult.getSize(), pageResult.getCurrent(), dtoList);

        return pageInfo;
    }

    @Override
    public List getListByDtoAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) throws Exception {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        List<WrapperPiece> queryFiledModelList = WrapperPiece.getQueryWrapperFromDto(dtoClassInfo, object);

        return getListByQueryFields(relationInfo, queryFiledModelList);
    }

    @Override
    public <T> List<T> getListByQueryFields(EntityDtoServiceRelation relationInfo
            , List<WrapperPiece> queryFiledModelList
    ) throws Exception {
        IService service = relationInfo.getServiceBean(applicationContext);
        List<T> dtoList = null;
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        TableInfo tableInfo = TableInfoHelper.getTableInfo(dtoClassInfo.getEntityClassInfo().getClazz());
        QueryWrapper qw = WrapperPiece.createQueryWrapper(queryFiledModelList);
        List entityList = null;
        if (dtoClassInfo.containchild()) {
            entityList = ((MybatisPlusMapper)service.getBaseMapper()).selectListWithJoin(qw, dtoClassInfo.getJoinTables(), dtoClassInfo.getJointColumns());
        } else {
            entityList = service.list(qw);
        }
        dtoList = (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityList, relationInfo.getDtoClass());;

        return dtoList;
    }
}
