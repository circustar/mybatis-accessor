package com.circustar.mvcenhance.enhance.update;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.enhance.utils.MybatisPlusUtils;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateEntity {
    private EntityDtoServiceRelation relation;
    private UpdateType updateType;
    private List<Object> objList;
    private QueryWrapper wrapper;

    public UpdateType getUpdateType() {
        return updateType;
    }

    public void setUpdateType(UpdateType updateType) {
        this.updateType = updateType;
    }

    public List<Object> getObjList() {
        return objList;
    }

    public void setObjList(List<Object> objList) {
        this.objList = objList;
    }

    public EntityDtoServiceRelation getRelation() {
        return relation;
    }

    public void setRelation(EntityDtoServiceRelation relation) {
        this.relation = relation;
    }

    public QueryWrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(QueryWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public boolean execUpdate(IService s) throws Exception {
        boolean result = false;
        if(updateType == UpdateType.INSERT) {
            result = s.saveBatch(objList);
        } else if(updateType == UpdateType.UPDATE_ID) {
            result = s.updateBatchById(objList);
        } else if(updateType == UpdateType.UPDATE_WRAPPER) {
            result = s.update(objList.get(0), (Wrapper) wrapper);
        } else if(updateType == UpdateType.DELETE_ID) {
            result = s.removeByIds(objList);
        } else if(updateType == UpdateType.PHYSIC_DELETE_ID) {
            result = MybatisPlusUtils.deleteBatchIds(s, objList.stream()
                    .map(x -> (Serializable)x).collect(Collectors.toList())
                    ,true);
        } else if(updateType == UpdateType.DELETE_WRAPPER) {
            result = s.remove(wrapper);
        } else if(updateType == UpdateType.PHYSIC_DELETE_WRAPPER) {
            result = MybatisPlusUtils.delete(s, wrapper, true);
        }

        return result;
    }
}
