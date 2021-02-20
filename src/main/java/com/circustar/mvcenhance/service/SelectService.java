package com.circustar.mvcenhance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.annotation.Selector;
import com.circustar.mvcenhance.classInfo.DtoField;
import com.circustar.mvcenhance.wrapper.WrapperPiece;
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
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

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
    public Object getById(EntityDtoServiceRelation relationInfo
            , Serializable id
            , String[] children) throws Exception {
        IService s = relationInfo.getServiceBean(applicationContext);
        Object oriEntity = s.getById(id);
        if (oriEntity == null) {
            return null;
        }
        Object result = this.dtoClassInfoHelper.convertFromEntity(oriEntity, relationInfo.getDtoClass());
        Set<String> childList;
        if(children == null) {
            childList = Collections.emptySet();
        } else {
            childList = new HashSet<>(Arrays.asList(children));
        }
        List<DtoField> subFields = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass())
                .getSubDtoFieldList().stream().filter(x -> childList.contains(x)).collect(Collectors.toList());

        Map<Boolean, List<DtoField>> dtoFieldMap = subFields.stream()
                .collect(Collectors.partitioningBy(x -> x.getSelectors() == null));

        DtoFields.queryAndAssignDtoFieldById(applicationContext, dtoClassInfoHelper, entityDtoServiceRelationMap
                , relationInfo, dtoFieldMap.get(true), result, id);

        DtoFields.queryAndAssignDtoFieldBySelector(applicationContext, dtoClassInfoHelper
                , entityDtoServiceRelationMap
                , relationInfo
                , result
                , dtoFieldMap.get(false));

        return result;
    }

    @Override
    public <T> PageInfo<T> getPagesByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
            , Integer page_index
            , Integer page_size
            ) throws Exception {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        List<WrapperPiece> queryWrapper = WrapperPiece.getQueryWrapperFromDto(dtoClassInfo, object);

        return getPagesByWrapper(relationInfo, queryWrapper, page_index, page_size);
    }

    @Override
    public <T> PageInfo<T> getPagesByWrapper(EntityDtoServiceRelation relationInfo
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

        if (!StringUtils.isEmpty(dtoClassInfo.getJoinTables())) {
            pageResult = ((MybatisPlusMapper) service.getBaseMapper()).selectPageWithJoin(page, qw, dtoClassInfo.getJoinTables(), dtoClassInfo.getJointColumns());
        } else {
            pageResult = service.page(page, qw);
        }
        List<T> dtoList = (List) dtoClassInfoHelper.convertFromEntityList(pageResult.getRecords(), relationInfo.getDtoClass());
        pageInfo = new PageInfo(pageResult.getTotal(), pageResult.getSize(), pageResult.getCurrent(), dtoList);

        return pageInfo;
    }

    @Override
    public List getListByAnnotation(EntityDtoServiceRelation relationInfo
            , Object object
    ) throws Exception {
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        List<WrapperPiece> queryFiledModelList = WrapperPiece.getQueryWrapperFromDto(dtoClassInfo, object);

        return getListByWrapper(relationInfo, queryFiledModelList);
    }

    @Override
    public <T> List<T> getListByWrapper(EntityDtoServiceRelation relationInfo
            , List<WrapperPiece> queryFiledModelList
    ) throws Exception {
        IService service = relationInfo.getServiceBean(applicationContext);
        List<T> dtoList = null;
        DtoClassInfo dtoClassInfo = this.dtoClassInfoHelper.getDtoClassInfo(relationInfo.getDtoClass());
        QueryWrapper qw = WrapperPiece.createQueryWrapper(queryFiledModelList);
        List entityList = null;
        if (!StringUtils.isEmpty(dtoClassInfo.getJoinTables())) {
            entityList = ((MybatisPlusMapper)service.getBaseMapper()).selectListWithJoin(qw, dtoClassInfo.getJoinTables(), dtoClassInfo.getJointColumns());
        } else {
            entityList = service.list(qw);
        }
        dtoList = (List<T>) this.dtoClassInfoHelper.convertFromEntityList(entityList, relationInfo.getDtoClass());;

        return dtoList;
    }
}
