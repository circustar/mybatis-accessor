package com.circustar.mvcenhance.enhance.update;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.enhance.utils.FieldUtils;
import com.circustar.mvcenhance.enhance.utils.MybatisPlusUtils;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateEntity {
    public static int DEFAULT_BATCH_LIMIT = 5;
    public UpdateEntity(EntityDtoServiceRelation relation
            , UpdateCommand updateCommand
            , IService service) {
        this(relation, updateCommand, service, true);
    }
    public UpdateEntity(EntityDtoServiceRelation relation
            , UpdateCommand updateCommand
            , IService service
            , Boolean subEntityUpdatePrior) {
        this.relation = relation;
        this.updateCommand = updateCommand;
        this.service = service;
        this.tableInfo = TableInfoHelper.getTableInfo(relation.getEntity());
        this.subEntityUpdatePrior = subEntityUpdatePrior;
    }
    private EntityDtoServiceRelation relation;
    private UpdateCommand updateCommand;
    private IService service;
    private TableInfo tableInfo;
    private Boolean subEntityUpdatePrior;

    private List<Object> objList;
    private QueryWrapper wrapper;
    private List<UpdateEntity> subUpdateEntities;

    public UpdateEntity addObject(Object obj) {
        if(objList == null) {objList = new ArrayList<>();};
        objList.add(obj);
        return this;
    }


    public void addSubUpdateEntity(UpdateEntity subUpdateEntity) {
        if(this.subUpdateEntities == null) {
            this.subUpdateEntities = new ArrayList<>();
        }
        this.subUpdateEntities.add(subUpdateEntity);
    }
    public void addSubUpdateEntities(List<UpdateEntity> subUpdateEntities) {
        if(this.subUpdateEntities == null) {
            this.subUpdateEntities = new ArrayList<>();
        }
        this.subUpdateEntities.addAll(subUpdateEntities);
    }
    public List<UpdateEntity> getSubUpdateEntities() {

        return subUpdateEntities;
    }

    public void setSubUpdateEntities(List<UpdateEntity> subUpdateEntities) {
        this.subUpdateEntities = subUpdateEntities;
    }

    public IService getService() {
        return service;
    }

    public void setService(IService service) {
        this.service = service;
    }

    public UpdateCommand getUpdateCommand() {
        return updateCommand;
    }

    public void setUpdateCommand(UpdateCommand updateCommand) {
        this.updateCommand = updateCommand;
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

    public boolean execUpdate(int BATCH_LIMIT) throws Exception {
        boolean result = true;
        if(subEntityUpdatePrior && subUpdateEntities != null) {
            for(UpdateEntity updateEntity : subUpdateEntities) {
                result = updateEntity.execUpdate(BATCH_LIMIT);
                if(!result) {
                    return false;
                }
            }
        }

        updatedEntityList = new ArrayList<>();
        if(objList != null && objList.size() > 0) {
            if (updateCommand == UpdateCommand.INSERT) {
                result = service.saveBatch(objList);
                updatedEntityList.addAll(objList);
            } else if (updateCommand == UpdateCommand.UPDATE_ID) {
                result = service.updateBatchById(objList);
                updatedEntityList.addAll(objList);
            } else if (updateCommand == UpdateCommand.UPDATE_WRAPPER && wrapper != null) {
                result = service.update(objList.get(0), (Wrapper) wrapper);
                updatedEntityList.add(objList.get(0));
            } else if (updateCommand == UpdateCommand.DELETE_ID) {
                if (objList.size() < BATCH_LIMIT) {
                    for (Object obj : objList) {
                        result = service.removeById((Serializable) obj);
                        if (!result) {
                            return false;
                        }
                    }
                    return true;
                }
                service.removeByIds(objList);
            } else if (updateCommand == UpdateCommand.PHYSIC_DELETE_ID) {
                if (objList.size() < BATCH_LIMIT) {
                    for (Object obj : objList) {
                        result = MybatisPlusUtils.deleteById(service, (Serializable) obj, true);
                        if (!result) {
                            return false;
                        }
                    }
                    return true;
                }
                MybatisPlusUtils.deleteBatchIds(service, objList.stream()
                                .map(x -> (Serializable) x).collect(Collectors.toList())
                        , true);
            } else if (updateCommand == UpdateCommand.SAVE_OR_UPDATE) {
                if (objList.size() < BATCH_LIMIT) {
                    for(Object obj : objList) {
                        result = service.saveOrUpdate(obj);
                    }
                } else {
                    result = service.saveOrUpdateBatch(objList);
                }
                updatedEntityList.addAll(objList);
            }
        }

        if(wrapper != null) {
            if (updateCommand == UpdateCommand.DELETE_WRAPPER) {
                result = service.remove(wrapper);
            } else if (updateCommand == UpdateCommand.PHYSIC_DELETE_WRAPPER) {
                result = MybatisPlusUtils.delete(service, wrapper, true);
            }
        }

        if((!subEntityUpdatePrior) && subUpdateEntities != null) {
            for(UpdateEntity subUpdateEntity : subUpdateEntities) {
                if(objList != null && objList.size() > 0) {
                    if(subUpdateEntity.getObjList() != null) {
                        for(Object x : subUpdateEntity.getObjList()) {
                            String keyProperty = tableInfo.getKeyProperty();
                            Object masterKey = FieldUtils.getValueByName(objList.get(0), keyProperty);
                            FieldUtils.setFieldByName(x, keyProperty, masterKey);
                        }
                    }
                }
                result = subUpdateEntity.execUpdate(UpdateEntity.DEFAULT_BATCH_LIMIT);
                if(!result) {
                    return false;
                }
            }
        }
        return result;
    }

    private List<Object> updatedEntityList;
    public List<Object> getUpdatedEntityList() {
        return updatedEntityList;
    }
}
