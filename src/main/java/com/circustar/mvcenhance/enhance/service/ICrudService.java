package com.circustar.mvcenhance.enhance.service;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import com.circustar.mvcenhance.enhance.update.CascadeUpdateStrategy;
import com.circustar.mvcenhance.enhance.update.UpdateEntity;

import java.io.Serializable;
import java.util.List;

public interface ICrudService {
    <T> boolean deleteByIds(EntityDtoServiceRelation relation, String idName, List<Serializable> ids, boolean physic, boolean cascade) throws Exception;
    <T> boolean deleteById(EntityDtoServiceRelation relation, String idName, Serializable id, boolean physic, boolean cascade) throws Exception;
    <T> boolean save(EntityDtoServiceRelation relation, TableInfo tableInfo, Object dto) throws Exception;
    <T> boolean update(EntityDtoServiceRelation relation, TableInfo tableInfo, Serializable idValue, Object dto, CascadeUpdateStrategy updateStrategy, List<String> assignedTargetList) throws Exception;
    <T> boolean updateStatus(List<UpdateEntity> objList) throws Exception;
}
