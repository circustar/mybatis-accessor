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
            , boolean updateChildrenOnly
            , boolean feedbackFlag) {
        this.option = option;
        this.updateCommand = updateCommand;
        this.service = service;
        this.updateTargets = updateTargets;
        this.updateChildFirst = updateChildrenFirst;
        this.entityClassInfo = entityClassInfo;
        this.updateChildrenOnly = updateChildrenOnly;
        this.feedbackFlag = feedbackFlag;
    }
    private Object option;
    private IUpdateCommand updateCommand;
    private IService service;
    private Boolean updateChildFirst;
    private List updateTargets;
    private List<IEntityUpdateProcessor> subUpdateEntities;
    private EntityClassInfo entityClassInfo;
    private boolean updateChildrenOnly;
    private boolean feedbackFlag;

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
        if(feedbackFlag) {
            return updateTargets;
        }
        return null;
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
                Object parentPropertyValue = keyMap.getOrDefault(entityClassInfo.getKeyField().getField().getName(), null);
                if (parentPropertyValue != null) {
                    avoidIdList = Arrays.asList(entityClassInfo.getKeyField().getField().getName()
                            , entityClassInfo.getIdReferenceFieldInfo().getField().getName());
                    for (Object updateEntity : updateTargets) {
                        FieldUtils.setFieldValueIfNull(updateEntity
                                , entityClassInfo.getIdReferenceFieldInfo().getReadMethod()
                                , entityClassInfo.getIdReferenceFieldInfo().getWriteMethod(), parentPropertyValue);
                    }
                }
            }

            for (String keyProperty : keyMap.keySet()) {
                if (avoidIdList != null && avoidIdList.contains(keyProperty)) {
                    continue;
                }
                EntityFieldInfo entityFieldInfo = entityClassInfo.getFieldByName(keyProperty);
                if (entityFieldInfo == null) {
                    continue;
                }
                Object keyValue = keyMap.get(keyProperty);
                for (Object updateEntity : updateTargets) {
                    FieldUtils.setFieldValueIfNull(updateEntity, entityFieldInfo.getReadMethod(), entityFieldInfo.getWriteMethod(), keyValue);
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
                    Object masterKeyValue = FieldUtils.getFieldValue(firstEntity.get(), keyField.getReadMethod());
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
