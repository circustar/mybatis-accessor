package com.circustar.mybatis_accessor.updateProcessor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.classInfo.EntityClassInfo;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.common_utils.reflection.FieldUtils;


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
        this.updateChildFirst = updateChildrenFirst;
        this.entityClassInfo = entityClassInfo;
        this.updateChildrenOnly = updateChildrenOnly;
    }
    private Object option;
    private IUpdateCommand updateCommand;
    private IService service;
    private Boolean updateChildFirst;
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
        if(subUpdateEntities == null) {
            return;
        }
        if(this.subUpdateEntities == null) {
            this.subUpdateEntities = new ArrayList<>();
        }
        this.subUpdateEntities.addAll(subUpdateEntities);
    }
    public List<IEntityUpdateProcessor> getSubUpdateEntities() {
        return subUpdateEntities;
    }

    public List getUpdatedTargets() {
        return updateTargets;
    }

    @Override
    public boolean execUpdate() {
        return execUpdate(new HashMap<String, Object>());
    }

    @Override
    public boolean execUpdate(Map<String, Object> keyMap) {
        boolean result;
        if (updateChildFirst && subUpdateEntities != null) {
            for (IEntityUpdateProcessor subDefaultEntityCollectionUpdater : subUpdateEntities) {
                result = subDefaultEntityCollectionUpdater.execUpdate(keyMap);
                if (!result) {
                    return false;
                }
            }
        }
        if (entityClassInfo != null) {
            List<String> avoidIdList = null;
            if (entityClassInfo.getKeyField() != null && entityClassInfo.getIdReferenceFieldInfo() != null) {
                Object parentPropertyValue = keyMap.get(entityClassInfo.getKeyField().getField().getName());
                if (parentPropertyValue != null) {
                    avoidIdList = Arrays.asList(entityClassInfo.getKeyField().getField().getName()
                            , entityClassInfo.getIdReferenceFieldInfo().getField().getName());
                    for (Object updateEntity : updateTargets) {
                        FieldUtils.setFieldValueIfNull(updateEntity
                                , entityClassInfo.getIdReferenceFieldInfo().getPropertyDescriptor().getReadMethod()
                                , entityClassInfo.getIdReferenceFieldInfo().getPropertyDescriptor().getWriteMethod(), parentPropertyValue);
                    }
                }
            }

            for (Map.Entry<String, Object> keyEntry : keyMap.entrySet()) {
                if (avoidIdList != null && avoidIdList.contains(keyEntry.getKey())) {
                    continue;
                }
                EntityFieldInfo entityFieldInfo = entityClassInfo.getFieldByName(keyEntry.getKey());
                if (entityFieldInfo == null) {
                    continue;
                }
                for (Object updateEntity : updateTargets) {
                    FieldUtils.setFieldValueIfNull(updateEntity, entityFieldInfo.getPropertyDescriptor().getReadMethod()
                            , entityFieldInfo.getPropertyDescriptor().getWriteMethod(), keyEntry.getValue());
                }
            }
        }
        if (!updateChildrenOnly) {
            result = this.updateCommand.update(this.service, this.updateTargets, option);
            if (!result) return false;
        }

        if (entityClassInfo != null) {
            Optional firstEntity = updateTargets.stream().findFirst();
            if (firstEntity.isPresent()) {
                EntityFieldInfo keyField = entityClassInfo.getKeyField();
                if (keyField != null) {
                    Object masterKeyValue = FieldUtils.getFieldValue(firstEntity.get()
                            , keyField.getPropertyDescriptor().getReadMethod());
                    keyMap.put(keyField.getField().getName(), masterKeyValue);
                }
            }
        }

        if ((!updateChildFirst) && subUpdateEntities != null) {
            for (IEntityUpdateProcessor subDefaultEntityCollectionUpdater : subUpdateEntities) {
                result = subDefaultEntityCollectionUpdater.execUpdate(new HashMap<>(keyMap));
                if (!result) {
                    return false;
                }
            }
        }
        return true;
    }
}
