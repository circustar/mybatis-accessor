package com.circustar.mybatis_accessor.update_processor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.listener.IListener;
import com.circustar.common_utils.listener.IListenerContext;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.EntityClassInfo;
import com.circustar.mybatis_accessor.class_info.EntityFieldInfo;
import com.circustar.mybatis_accessor.listener.UpdateProcessorDecodeListener;
import com.circustar.mybatis_accessor.listener.UpdateProcessorPropertyChangeListener;
import com.circustar.mybatis_accessor.listener.UpdateProcessorUpdateListener;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.common_utils.reflection.FieldUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

public abstract class AbstractDtoUpdateProcessor implements IEntityUpdateProcessor<Collection>, IListenerContext<AbstractDtoUpdateProcessor>  {
    private final Object option;
    private final IUpdateCommand updateCommand;
    private final IService service;
    private final Boolean updateChildFirst;
    private final List updateDtoList;
    private List updateEntityList;
    private List<IEntityUpdateProcessor> subUpdateEntities;
    private final DtoClassInfo dtoClassInfo;
    private final EntityClassInfo entityClassInfo;
    private final boolean updateChildrenOnly;
    private List<IListener<AbstractDtoUpdateProcessor>> listenerList;

    public AbstractDtoUpdateProcessor(IService service
            , IUpdateCommand updateCommand
            , Object option
            , DtoClassInfo dtoClassInfo
            , List updateDtoList
            , Boolean updateChildrenFirst
            , boolean updateChildrenOnly) {
        this.option = option;
        this.updateCommand = updateCommand;
        this.service = service;
        this.updateDtoList = updateDtoList;
        this.updateChildFirst = updateChildrenFirst;
        this.dtoClassInfo = dtoClassInfo;
        this.entityClassInfo = dtoClassInfo.getEntityClassInfo();
        this.updateChildrenOnly = updateChildrenOnly;
        this.updateEntityList = null;
    }

    public List getUpdateDtoList() {
        return updateDtoList;
    }

    public IUpdateCommand getUpdateCommand() {
        return updateCommand;
    }

    public DtoClassInfo getDtoClassInfo() {
        return dtoClassInfo;
    }

    public void addSubUpdateEntity(AbstractDtoUpdateProcessor subDefaultEntityCollectionUpdater) {
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

    @Override
    public List getUpdatedEntityList() {
        return updateEntityList;
    }

    @Override
    public boolean execUpdate(String updateEventLogId) throws MybatisAccessorException {
        String var0 = updateEventLogId;
        if(!StringUtils.hasLength(var0)) {
            var0 = UUID.randomUUID().toString();
        }
        return execUpdate(new HashMap<>(), new ArrayList<>(), var0, 0);
    }

    public List convertToEntity() {
        return dtoClassInfo.getDtoClassInfoHelper().convertToEntityList(this.updateDtoList
                , dtoClassInfo, false);
    }

    public Serializable getUpdateKey(Object dto) {
        Method keyFieldReadMethod = dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();
        return (Serializable) FieldUtils.getFieldValue(dto, keyFieldReadMethod);
    }

    @Override
    public boolean execUpdate(Map<String, Object> keyMap, List<Supplier<Integer>> consumerList, String updateEventLogId, int level) throws MybatisAccessorException {
        init(this);
        boolean result;

        if (updateChildFirst) {
            result = execSubEntityUpdate(keyMap, consumerList, updateEventLogId, level);
            if(!result){return false;}
        }
        if (!updateChildrenOnly) {
            this.execListeners(ExecuteTiming.BEFORE_ENTITY_UPDATE, updateEventLogId, level);
        }
        if(this.updateEntityList == null) {
            this.updateEntityList = convertToEntity();
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
                    Method keyFieldWriteMethod = entityClassInfo.getIdReferenceFieldInfo().getPropertyDescriptor().getWriteMethod();
                    for (Object updateEntity : updateEntityList) {
                        FieldUtils.setFieldValue(updateEntity
                                , keyFieldWriteMethod
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
            if (!result) {return false;}
            if(IUpdateCommand.UpdateType.INSERT.equals(this.updateCommand.getUpdateType())) {
                Method keyFieldWriteMethod = this.dtoClassInfo.getKeyField().getPropertyDescriptor().getWriteMethod();
                final Method keyFieldReadMethod = entityClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();
                for(int i = 0; i < this.updateEntityList.size(); i++) {
                    FieldUtils.setFieldValue(this.updateDtoList.get(i)
                            , keyFieldWriteMethod
                            , FieldUtils.getFieldValue(updateEntityList.get(i), keyFieldReadMethod));
                }
            }
            this.execListeners(ExecuteTiming.AFTER_ENTITY_UPDATE, updateEventLogId, level);
        }

        if (entityClassInfo != null && firstEntity.isPresent()
                && entityClassInfo.getEntityClass().isAssignableFrom(firstEntity.get().getClass())) {
            EntityFieldInfo keyField = entityClassInfo.getKeyField();
            if (keyField != null) {
                Object masterKeyValue = FieldUtils.getFieldValue(firstEntity.get()
                        , keyField.getPropertyDescriptor().getReadMethod());
                if(masterKeyValue != null) {
                    keyMap.put(keyField.getField().getName(), masterKeyValue);
                }
            }
        }

        if (!updateChildFirst) {
            result = execSubEntityUpdate(keyMap, consumerList, updateEventLogId, level);
            if(!result){return false;}
        }

        this.dispose();
        return true;
    }

    private boolean execSubEntityUpdate(Map<String, Object> keyMap, List<Supplier<Integer>> consumerList
            , String updateEventLogId, int level) throws MybatisAccessorException {
        this.execListeners(ExecuteTiming.BEFORE_SUB_ENTITY_UPDATE, updateEventLogId, level);
        if(this.subUpdateEntities != null && !this.subUpdateEntities.isEmpty()) {
            if(!this.skipAllListener(ExecuteTiming.AFTER_SUB_ENTITY_UPDATE)) {
                consumerList.add(() -> {
                    try {
                        this.execListeners(ExecuteTiming.AFTER_SUB_ENTITY_UPDATE, updateEventLogId, level);
                    } catch (MybatisAccessorException e) {
                        throw new RuntimeException(e);
                    }
                    return level;
                });
            }
            for (IEntityUpdateProcessor entityUpdateProcessor : subUpdateEntities) {
                boolean result = entityUpdateProcessor.execUpdate(new HashMap<>(keyMap), consumerList, updateEventLogId, level + 1);
                if (!result) {
                    return false;
                }
            }
        }

        if(level == 0) {
            int size = consumerList.size();
            for(int i = 0; i < size; i++) {
                consumerList.get(i).get();
            }
        }
        return true;
    }

    @Override
    public void init(AbstractDtoUpdateProcessor target) {
        UpdateProcessorUpdateListener updateListener = new UpdateProcessorUpdateListener(this);
        updateListener.init();
        UpdateProcessorPropertyChangeListener changeListener = new UpdateProcessorPropertyChangeListener(this);
        changeListener.init();
        UpdateProcessorDecodeListener decodeListener = new UpdateProcessorDecodeListener(this);
        decodeListener.init();
        this.listenerList = Arrays.asList(decodeListener, updateListener, changeListener);
    }

    @Override
    public AbstractDtoUpdateProcessor getListenTarget() {
        return this;
    }

    @Override
    public List<IListener<AbstractDtoUpdateProcessor>> getListenerList() {
        return this.listenerList;
    }
}
