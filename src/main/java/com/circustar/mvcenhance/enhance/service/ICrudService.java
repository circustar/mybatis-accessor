package com.circustar.mvcenhance.enhance.service;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.circustar.mvcenhance.enhance.field.SubFieldInfo;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.update.UpdateSubEntityStrategy;
import com.circustar.mvcenhance.enhance.update.UpdateEntity;

import java.io.Serializable;
import java.util.List;

public interface ICrudService {
    <T> void deleteByIds(EntityDtoServiceRelation relation, String idName, List<Serializable> ids, List<String> subEntities, boolean physicDelete) throws Exception;
    <T> void deleteById(EntityDtoServiceRelation relation, String idName, Serializable id, List<String> subEntities, boolean physicDelete) throws Exception;
    <T> void save(EntityDtoServiceRelation relation, Object dto, List<String> subEntityList) throws Exception;
    <T> void saveList(EntityDtoServiceRelation relation, List<Object> dtoList) throws Exception;
    <T> void update(EntityDtoServiceRelation relation, Serializable idValue, Object dto
            , List<String> assignedTargetList, UpdateSubEntityStrategy updateStrategy, boolean physicDelete) throws Exception;
    <T> void saveOrUpdateOrDeleteList(EntityDtoServiceRelation relation, List<Object> dtoList, boolean physicDelete) throws Exception;
    <T> void updateSubEntityList(EntityDtoServiceRelation relation, Serializable idValue, EntityDtoServiceRelation subFieldRelation
            , List<Object> subEntity, UpdateSubEntityStrategy updateStrategy, boolean physicDelete) throws Exception;
    <T> void businessUpdate(List<UpdateEntity> objList) throws Exception;
}
