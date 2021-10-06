package com.circustar.mybatis_accessor.updateProcessor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.parser.SPELParser;
import com.circustar.mybatis_accessor.annotation.after_update.AfterUpdateModel;
import com.circustar.mybatis_accessor.annotation.after_update.ExecuteTiming;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.EntityClassInfo;
import com.circustar.mybatis_accessor.classInfo.EntityFieldInfo;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.common_utils.reflection.FieldUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DefaultEntityCollectionUpdateProcessor implements IEntityUpdateProcessor<Collection> {
    public DefaultEntityCollectionUpdateProcessor(IService service
            , IUpdateCommand updateCommand
            , Object option
            , DtoClassInfo dtoClassInfo
            , List updateDtoList
            , List updateEntityList
            , Boolean updateChildrenFirst
            , boolean updateChildrenOnly) {
        this.option = option;
        this.updateCommand = updateCommand;
        this.service = service;
        this.updateDtoList = updateDtoList;
        this.updateEntityList = updateEntityList;
        this.updateChildFirst = updateChildrenFirst;
        this.dtoClassInfo = dtoClassInfo;
        this.entityClassInfo = dtoClassInfo.getEntityClassInfo();
        this.updateChildrenOnly = updateChildrenOnly;
    }
    private Object option;
    private IUpdateCommand updateCommand;
    private IService service;
    private Boolean updateChildFirst;
    private List updateDtoList;
    private List updateEntityList;
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

    public List getUpdatedEntityList() {
        return updateEntityList;
    }

    @Override
    public boolean execUpdate() {
        return execUpdate(new HashMap<>(), new ArrayList<>(), 0);
    }

    private List<AfterUpdateModel> getAfterUpdateList () {
        return this.dtoClassInfo.getAfterUpdateList();
    }

    @Override
    public boolean execUpdate(Map<String, Object> keyMap, List<Supplier<Integer>> consumerList, int level) {
        boolean result;
        List<AfterUpdateModel> afterUpdateList = this.getAfterUpdateList();
        executeAfterUpdateExecutor(dtoClassInfo, afterUpdateList, ExecuteTiming.BEFORE_UPDATE, updateCommand.getUpdateType()
                , this.updateDtoList, this.updateEntityList);

        if (updateChildFirst) {
            execSubEntityUpdate(keyMap, consumerList, level);
        }
        Optional firstEntity = updateEntityList.stream().findFirst();
        if (entityClassInfo != null && firstEntity.isPresent()
                && entityClassInfo.getEntityClass().isAssignableFrom(firstEntity.get().getClass())) {
            List<String> avoidIdList = null;
            if (entityClassInfo.getKeyField() != null && entityClassInfo.getIdReferenceFieldInfo() != null) {
                Object parentPropertyValue = keyMap.get(entityClassInfo.getKeyField().getField().getName());
                if (parentPropertyValue != null) {
                    avoidIdList = Arrays.asList(entityClassInfo.getKeyField().getField().getName()
                            , entityClassInfo.getIdReferenceFieldInfo().getField().getName());
                    for (Object updateEntity : updateEntityList) {
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
                for (Object updateEntity : updateEntityList) {
                    FieldUtils.setFieldValue(updateEntity
                            , entityFieldInfo.getPropertyDescriptor().getWriteMethod(), keyEntry.getValue());
                }
            }
        }
        if (!updateChildrenOnly) {
            result = this.updateCommand.update(this.service, this.updateEntityList, option);
            if (!result) return false;

            executeAfterUpdateExecutor(dtoClassInfo, afterUpdateList, ExecuteTiming.AFTER_ENTITY_UPDATE, updateCommand.getUpdateType()
                    , this.updateDtoList, this.updateEntityList);
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

        if ((!updateChildFirst)) {
            execSubEntityUpdate(keyMap, consumerList, level);
        }
        if(level == 0) {
            for(int i = 0; i < consumerList.size(); i++) {
                consumerList.get(i).get();
            }
        }
        return true;
    }

    private boolean execSubEntityUpdate(Map<String, Object> keyMap, List<Supplier<Integer>> consumerList, int level) {
        consumerList.add(() -> {
            executeAfterUpdateExecutor(this.dtoClassInfo, this.getAfterUpdateList()
                    , ExecuteTiming.AFTER_SUB_ENTITY_UPDATE, this.updateCommand.getUpdateType()
                    , this.updateDtoList, this.updateEntityList);
            return level;
        });
        if(this.subUpdateEntities == null) {
            return true;
        }
        boolean result = false;
        for (IEntityUpdateProcessor entityUpdateProcessor : subUpdateEntities) {
            result = entityUpdateProcessor.execUpdate(new HashMap<>(keyMap), consumerList, level + 1);
            if (!result) {
                break;
            }
        }
        return result;
    }

    private void executeAfterUpdateExecutor(DtoClassInfo dtoClassInfo
            , List<AfterUpdateModel> afterUpdateList
            , ExecuteTiming executeTiming, IUpdateCommand.UpdateType updateType
            , List updateDtoList, List updateEntityList) {
        if(afterUpdateList == null || afterUpdateList.isEmpty()) {
            return;
        }
        List<AfterUpdateModel> updateModelList = afterUpdateList.stream()
                .filter(x -> executeTiming.equals(x.getAfterUpdateExecutor().getExecuteTiming()))
                .filter(x -> Arrays.stream(x.getUpdateTypes()).anyMatch(y -> updateType.equals(y)))
                .collect(Collectors.toList());
        for(AfterUpdateModel m : updateModelList) {
            List executeDtoList = new ArrayList();
            List executeEntityList = new ArrayList();
            for(int i = 0 ; i < updateDtoList.size(); i++) {
                boolean execFlag = true;
                if(StringUtils.hasLength(m.getOnExpression())) {
                    execFlag = (boolean) SPELParser.parseExpression(updateDtoList.get(i),m.getOnExpression());
                }
                if(!execFlag) {
                    continue;
                }
                executeDtoList.add(updateDtoList.get(i));
                executeEntityList.add(updateEntityList.get(i));
            }
            if(!executeDtoList.isEmpty()) {
                m.getAfterUpdateExecutor().exec(dtoClassInfo, executeDtoList, executeEntityList, m.getUpdateParams());
            }
        }
    }
}
