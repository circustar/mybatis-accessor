package com.circustar.mvcenhance.provider;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.field.EntityClassInfo;
import com.circustar.mvcenhance.field.FieldTypeInfo;
import com.circustar.mvcenhance.provider.command.IUpdateCommand;
import com.circustar.mvcenhance.utils.FieldUtils;

import java.util.*;

public class UpdateEntity {
    public UpdateEntity(IService service
            , IUpdateCommand updateCommand
            , Object option
            , EntityClassInfo entityClassInfo
            , Collection updateEntities
            , Boolean updateChildrenFirst
            , boolean updateChildrenOnly) {
        this.option = option;
        this.updateCommand = updateCommand;
        this.service = service;
        this.updateEntities = updateEntities;
        this.updateSubEntityFirst = updateChildrenFirst;
        this.entityClassInfo = entityClassInfo;
        this.updateChildrenOnly = updateChildrenOnly;
    }
    private Object option;
    private IUpdateCommand updateCommand;
    private IService service;
    private Boolean updateSubEntityFirst;
    private Collection updateEntities;
    private List<UpdateEntity> subUpdateEntities;
    private EntityClassInfo entityClassInfo;
    private boolean updateChildrenOnly;

    public void addSubUpdateEntity(UpdateEntity subUpdateEntity) {
        if(this.subUpdateEntities == null) {
            this.subUpdateEntities = new ArrayList<>();
        }
        this.subUpdateEntities.add(subUpdateEntity);
    }
    public void addSubUpdateEntities(Collection<UpdateEntity> subUpdateEntities) {
        if(this.subUpdateEntities == null) {
            this.subUpdateEntities = new ArrayList<>();
        }
        this.subUpdateEntities.addAll(subUpdateEntities);
    }
    public List<UpdateEntity> getSubUpdateEntities() {
        return subUpdateEntities;
    }

    public Collection getUpdateEntities() {
        return updateEntities;
    }

    public boolean execUpdate() throws Exception {
        return execUpdate(new HashMap<String, Object>());
    }

    protected boolean execUpdate(Map<String, Object> keyMap) throws Exception {
        boolean result = true;
        if(updateSubEntityFirst && subUpdateEntities != null) {
            for(UpdateEntity subUpdateEntity : subUpdateEntities) {
                result = subUpdateEntity.execUpdate(keyMap);
                if(!result) {
                    return false;
                }
            }
        }
        for(String keyProperty : keyMap.keySet()) {
            FieldTypeInfo fieldTypeInfo = entityClassInfo.getFieldByName(keyProperty);
            if(fieldTypeInfo == null) {
                continue;
            }
            Object keyValue = keyMap.get(keyProperty);
            for (Object updateEntity : updateEntities) {
                FieldUtils.setField(updateEntity, fieldTypeInfo.getField(), keyValue);
            }
        }
        if(!updateChildrenOnly) {
            result = this.updateCommand.update(this.service, this.updateEntities, option);
            if (!result) return false;
        }

        Optional firstEntity = updateEntities.stream().findFirst();
        if(firstEntity.isPresent()) {
            String keyProperty = entityClassInfo.getTableInfo().getKeyProperty();
            Object masterKeyValue = FieldUtils.getValueByName(firstEntity.get(), keyProperty);
            if(!keyMap.containsKey(keyProperty)) {
                keyMap.put(keyProperty, masterKeyValue);
            }
        }

        if((!updateSubEntityFirst) && subUpdateEntities != null) {
            for(UpdateEntity subUpdateEntity : subUpdateEntities) {
                result = subUpdateEntity.execUpdate(keyMap);
                if(!result) {
                    return false;
                }
            }
        }
        return result;
    }
}
