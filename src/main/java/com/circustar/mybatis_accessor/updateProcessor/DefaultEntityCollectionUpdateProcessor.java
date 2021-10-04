package com.circustar.mybatis_accessor.updateProcessor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.parser.SPELParser;
import com.circustar.mybatis_accessor.annotation.after_update_executor.AfterUpdateModel;
import com.circustar.mybatis_accessor.annotation.after_update_executor.ExecuteTiming;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.EntityClassInfo;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.common_utils.reflection.FieldUtils;
import org.springframework.util.StringUtils;


import java.util.*;
import java.util.stream.Collectors;

public class DefaultEntityCollectionUpdateProcessor implements IEntityUpdateProcessor<Collection> {
    public DefaultEntityCollectionUpdateProcessor(IService service
            , IUpdateCommand updateCommand
            , Object option
            , DtoClassInfo dtoClassInfo
            , List updateTargets
            , Boolean updateChildrenFirst
            , boolean updateChildrenOnly) {
        this.option = option;
        this.updateCommand = updateCommand;
        this.service = service;
        this.updateTargets = updateTargets;
        this.updateChildFirst = updateChildrenFirst;
        this.dtoClassInfo = dtoClassInfo;
        this.entityClassInfo = dtoClassInfo.getEntityClassInfo();
        this.updateChildrenOnly = updateChildrenOnly;
    }
    private Object option;
    private IUpdateCommand updateCommand;
    private IService service;
    private Boolean updateChildFirst;
    private List updateTargets;
    private List<IEntityUpdateProcessor> subUpdateEntities;
    private DtoClassInfo dtoClassInfo;
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
        return execUpdate(new HashMap<>());
    }

    @Override
    public boolean execUpdate(Map<String, Object> keyMap) {
        boolean result;
        List<AfterUpdateModel> afterUpdateList = this.dtoClassInfo.getAfterUpdateList();
        if(afterUpdateList != null && !afterUpdateList.isEmpty()) {
            executeAfterUpdateExecutor(dtoClassInfo, afterUpdateList, ExecuteTiming.BEFORE_UPDATE, updateCommand.getUpdateType()
                    , this.updateTargets);
        }

        if (updateChildFirst && subUpdateEntities != null) {
            for (IEntityUpdateProcessor subDefaultEntityCollectionUpdater : subUpdateEntities) {
                result = subDefaultEntityCollectionUpdater.execUpdate(keyMap);
                if (!result) {
                    return false;
                }
            }
        }
        Optional firstEntity = updateTargets.stream().findFirst();
        if (entityClassInfo != null && firstEntity.isPresent()
                && entityClassInfo.getEntityClass().isAssignableFrom(firstEntity.get().getClass())) {
            List<String> avoidIdList = null;
            if (entityClassInfo.getKeyField() != null && entityClassInfo.getIdReferenceFieldInfo() != null) {
                Object parentPropertyValue = keyMap.get(entityClassInfo.getKeyField().getField().getName());
                if (parentPropertyValue != null) {
                    avoidIdList = Arrays.asList(entityClassInfo.getKeyField().getField().getName()
                            , entityClassInfo.getIdReferenceFieldInfo().getField().getName());
                    for (Object updateEntity : updateTargets) {
                        FieldUtils.setFieldValue(updateEntity
                                , entityClassInfo.getIdReferenceFieldInfo().getPropertyDescriptor().getWriteMethod()
                                , parentPropertyValue);
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
                    FieldUtils.setFieldValue(updateEntity
                            , entityFieldInfo.getPropertyDescriptor().getWriteMethod(), keyEntry.getValue());
                }
            }
        }
        if (!updateChildrenOnly) {
            result = this.updateCommand.update(this.service, this.updateTargets, option);
            if (!result) return false;
        }

        if (entityClassInfo != null && firstEntity.isPresent()
                && entityClassInfo.getEntityClass().isAssignableFrom(firstEntity.get().getClass())) {
            EntityFieldInfo keyField = entityClassInfo.getKeyField();
            if (keyField != null) {
                Object masterKeyValue = FieldUtils.getFieldValue(firstEntity.get()
                        , keyField.getPropertyDescriptor().getReadMethod());
                keyMap.put(keyField.getField().getName(), masterKeyValue);
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
        if(afterUpdateList != null && !afterUpdateList.isEmpty()) {
            executeAfterUpdateExecutor(dtoClassInfo, afterUpdateList, ExecuteTiming.AFTER_UPDATE, updateCommand.getUpdateType()
                    , this.updateTargets);
        }
        return true;
    }

    private void executeAfterUpdateExecutor(DtoClassInfo dtoClassInfo
            , List<AfterUpdateModel> afterUpdateList
            , ExecuteTiming executeTiming, IUpdateCommand.UpdateType updateType, List updateTargets) {
        if(afterUpdateList == null || afterUpdateList.isEmpty()) {
            return;
        }
        List<AfterUpdateModel> updateModelList = afterUpdateList.stream()
                .filter(x -> executeTiming.equals(x.getAfterUpdateExecutor().getExecuteTiming()))
                .filter(x -> Arrays.stream(x.getUpdateTypes()).anyMatch(y -> updateType.equals(y)))
                .collect(Collectors.toList());
        for(AfterUpdateModel m : updateModelList) {
            List executeList = new ArrayList();
            for(Object o : updateTargets) {
                boolean execFlag = true;
                if(StringUtils.hasLength(m.getOnExpression())) {
                    execFlag = (boolean) SPELParser.parseExpression(o,m.getOnExpression());
                }
                if(!execFlag) {
                    continue;
                }
                executeList.add(o);
            }
            m.getAfterUpdateExecutor().exec(dtoClassInfo
                    , executeList, m.getUpdateParams());
        }
    }
}
