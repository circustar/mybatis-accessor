package com.circustar.mvcenhance.provider;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.classInfo.EntityClassInfo;
import com.circustar.mvcenhance.classInfo.FieldTypeInfo;
import com.circustar.mvcenhance.provider.command.IUpdateCommand;
import com.circustar.mvcenhance.utils.FieldUtils;

import java.util.*;

public class DefaultEntityCollectionUpdater implements IEntityUpdater<Collection> {
    public DefaultEntityCollectionUpdater(IService service
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
        this.updatechildFirst = updateChildrenFirst;
        this.entityClassInfo = entityClassInfo;
        this.updateChildrenOnly = updateChildrenOnly;
    }
    private Object option;
    private IUpdateCommand updateCommand;
    private IService service;
    private Boolean updatechildFirst;
    private Collection updateEntities;
    private List<DefaultEntityCollectionUpdater> subUpdateEntities;
    private EntityClassInfo entityClassInfo;
    private boolean updateChildrenOnly;

    public void addSubUpdateEntity(DefaultEntityCollectionUpdater subDefaultEntityCollectionUpdater) {
        if(this.subUpdateEntities == null) {
            this.subUpdateEntities = new ArrayList<>();
        }
        this.subUpdateEntities.add(subDefaultEntityCollectionUpdater);
    }
    public void addSubUpdateEntities(Collection<DefaultEntityCollectionUpdater> subUpdateEntities) {
        if(this.subUpdateEntities == null) {
            this.subUpdateEntities = new ArrayList<>();
        }
        this.subUpdateEntities.addAll(subUpdateEntities);
    }
    public List<DefaultEntityCollectionUpdater> getSubUpdateEntities() {
        return subUpdateEntities;
    }

    public Collection getUpdateEntities() {
        return updateEntities;
    }

    @Override
    public boolean execUpdate() throws Exception {
        return execUpdate(new HashMap<String, Object>());
    }

    @Override
    public boolean execUpdate(Map<String, Object> keyMap) throws Exception {
        boolean result = true;
        if(updatechildFirst && subUpdateEntities != null) {
            for(DefaultEntityCollectionUpdater subDefaultEntityCollectionUpdater : subUpdateEntities) {
                result = subDefaultEntityCollectionUpdater.execUpdate(keyMap);
                if(!result) {
                    return false;
                }
            }
        }
        if(entityClassInfo != null) {
            for (String keyProperty : keyMap.keySet()) {
                FieldTypeInfo fieldTypeInfo = entityClassInfo.getFieldByName(keyProperty);
                if (fieldTypeInfo == null) {
                    continue;
                }
                Object keyValue = keyMap.get(keyProperty);
                for (Object updateEntity : updateEntities) {
                    FieldUtils.setField(updateEntity, fieldTypeInfo.getField(), keyValue);
                }
            }
        }
        if(!updateChildrenOnly) {
            result = this.updateCommand.update(this.service, this.updateEntities, option);
            if (!result) return false;
        }

        if(entityClassInfo != null) {
            Optional firstEntity = updateEntities.stream().findFirst();
            if (firstEntity.isPresent()) {
                String keyProperty = entityClassInfo.getTableInfo().getKeyProperty();
                Object masterKeyValue = FieldUtils.getValueByName(firstEntity.get(), keyProperty);
                if (!keyMap.containsKey(keyProperty)) {
                    keyMap.put(keyProperty, masterKeyValue);
                }
            }
        }

        if((!updatechildFirst) && subUpdateEntities != null) {
            for(DefaultEntityCollectionUpdater subDefaultEntityCollectionUpdater : subUpdateEntities) {
                result = subDefaultEntityCollectionUpdater.execUpdate(keyMap);
                if(!result) {
                    return false;
                }
            }
        }
        return result;
    }
}
