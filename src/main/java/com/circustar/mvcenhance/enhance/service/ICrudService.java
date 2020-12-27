package com.circustar.mvcenhance.enhance.service;

import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface ICrudService {
//    void deleteByIds(EntityDtoServiceRelation relation, String idName, List<Serializable> ids, List<String> subEntities, boolean physicDelete) throws Exception;
//    void deleteById(EntityDtoServiceRelation relation, String idName, Serializable id, List<String> subEntities, boolean physicDelete) throws Exception;
//    void save(EntityDtoServiceRelation relation, Object dto, List<String> subEntityList) throws Exception;
//    void saveList(EntityDtoServiceRelation relation, List<Object> dtoList) throws Exception;
//    void update(EntityDtoServiceRelation relation, Serializable idValue, Object dto
//            , List<String> assignedTargetList, UpdateSubEntityStrategy updateStrategy, boolean physicDelete) throws Exception;
//    void saveOrUpdateOrDeleteList(EntityDtoServiceRelation relation, List<Object> dtoList, boolean physicDelete) throws Exception;
//    void updateSubEntityList(EntityDtoServiceRelation relation, Serializable idValue, EntityDtoServiceRelation subFieldRelation
//            , List<Object> subEntity, UpdateSubEntityStrategy updateStrategy, boolean physicDelete) throws Exception;
//    void businessUpdate(List<UpdateEntity> objList) throws Exception;
    List<Object> updateByProviders(EntityDtoServiceRelation relationInfo, Object object, String[] updateNames, Object[] options, BindingResult bindingResult) throws Exception;
}
