package org.yxy.circustar.mvc.enhance.service;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.yxy.circustar.mvc.enhance.update.UpdateEntity;
import org.yxy.circustar.mvc.enhance.update.CascadeUpdateStrategy;
import org.yxy.circustar.mvc.enhance.relation.EntityDtoServiceRelation;

import java.io.Serializable;
import java.util.List;

public interface ICrudService {
    <T> boolean deleteByIds(EntityDtoServiceRelation relation, String idName, List<Serializable> ids, boolean physic, boolean cascade) throws Exception;
    <T> boolean deleteById(EntityDtoServiceRelation relation, String idName, Serializable id, boolean physic, boolean cascade) throws Exception;
    <T> boolean save(EntityDtoServiceRelation relation, TableInfo tableInfo, Object dto) throws Exception;
    <T> boolean update(EntityDtoServiceRelation relation, TableInfo tableInfo, Serializable idValue, Object dto, CascadeUpdateStrategy updateStrategy, List<String> assignedTargetList) throws Exception;
    <T> boolean updateStatus(List<UpdateEntity> objList) throws Exception;
}
