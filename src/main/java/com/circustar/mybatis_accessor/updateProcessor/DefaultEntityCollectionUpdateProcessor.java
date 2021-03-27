package com.circustar.mybatis_accessor.updateProcessor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.classInfo.EntityClassInfo;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.utils.FieldUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class DefaultEntityCollectionUpdateProcessor implements IEntityUpdateProcessor<Collection> {
    public DefaultEntityCollectionUpdateProcessor(IService service
            , IUpdateCommand updateCommand
            , Object option
            , EntityClassInfo entityClassInfo
            , List updateTargets
            , Boolean updateChildrenFirst
            , boolean updateChildrenOnly) {
        this.option = option;
        this.updateCommand = updateCommand;
        this.service = service;
        this.updateTargets = updateTargets;
        this.updatechildFirst = updateChildrenFirst;
        this.entityClassInfo = entityClassInfo;
        this.updateChildrenOnly = updateChildrenOnly;
    }
    private Object option;
    private IUpdateCommand updateCommand;
    private IService service;
    private Boolean updatechildFirst;
    private List updateTargets;
    private List<IEntityUpdateProcessor> subUpdateEntities;
    private EntityClassInfo entityClassInfo;
    private boolean updateChildrenOnly;

    public void addSubUpdateEntity(DefaultEntityCollectionUpdateProcessor subDefaultEntityCollectionUpdater) {
        if(this.subUpdateEntities == null) {
            this.subUpdateEntities = new ArrayList<>();
        }
        this.subUpdateEntities.add(subDefaultEntityCollectionUpdater);
    }
    public void addSubUpdateEntities(Collection<IEntityUpdateProcessor> subUpdateEntities) {
        if(this.subUpdateEntities == null) {
            this.subUpdateEntities = new ArrayList<>();
        }
        this.subUpdateEntities.addAll(subUpdateEntities);
    }
    public List<IEntityUpdateProcessor> getSubUpdateEntities() {
        return subUpdateEntities;
    }

    public List getUpdateTargets() {
        return updateTargets;
    }

    @Override
    @Transactional
    public boolean execUpdate() throws Exception {
        return execUpdate(new HashMap<String, Object>());
    }

    @Override
    @Transactional
    public boolean execUpdate(Map<String, Object> keyMap) throws Exception {
        boolean result = true;
        if(updatechildFirst && subUpdateEntities != null) {
            for(IEntityUpdateProcessor subDefaultEntityCollectionUpdater : subUpdateEntities) {
                result = subDefaultEntityCollectionUpdater.execUpdate(keyMap);
                if(!result) {
                    return false;
                }
            }
        }
        if(entityClassInfo != null) {
            for (String keyProperty : keyMap.keySet()) {
                EntityFieldInfo entityFieldInfo = entityClassInfo.getFieldByName(keyProperty);
                if (entityFieldInfo == null) {
                    continue;
                }
                Object keyValue = keyMap.get(keyProperty);
                for (Object updateEntity : updateTargets) {
                    FieldUtils.setField(updateEntity, entityFieldInfo.getWriteMethod(), keyValue);
                }
            }
        }
        if(!updateChildrenOnly) {
            result = this.updateCommand.update(this.service, this.updateTargets, option);
            if (!result) return false;
        }

        if(entityClassInfo != null) {
            Optional firstEntity = updateTargets.stream().findFirst();
            if (firstEntity.isPresent()) {
                EntityFieldInfo keyField = entityClassInfo.getKeyField();
                if(keyField != null) {
                    Object masterKeyValue = FieldUtils.getValue(firstEntity.get(), keyField.getReadMethod());
                    if (!keyMap.containsKey(keyField.getField().getName())) {
                        keyMap.put(keyField.getField().getName(), masterKeyValue);
                    }
                }
            }
        }

        if((!updatechildFirst) && subUpdateEntities != null) {
            for(IEntityUpdateProcessor subDefaultEntityCollectionUpdater : subUpdateEntities) {
                result = subDefaultEntityCollectionUpdater.execUpdate(new HashMap<>(keyMap));
                if(!result) {
                    return false;
                }
            }
        }
        return result;
    }
}
