package com.circustar.mybatis_accessor.listener;

import com.circustar.common_utils.listener.IListener;
import com.circustar.common_utils.listener.IListenerTiming;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.listener.event.PropertyChangeEventModel;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateProcessorPropertyChangeListener implements IListener<DefaultEntityCollectionUpdateProcessor> {
    private List<PropertyChangeEventModel> onChangeList;
    private IUpdateCommand updateCommand;
    private DtoClassInfo dtoClassInfo;
    private List updateDtoList;

    public UpdateProcessorPropertyChangeListener(List<PropertyChangeEventModel> onChangeList
            , IUpdateCommand updateCommand
            , DtoClassInfo dtoClassInfo
            , List updateDtoList) {
        this.onChangeList = onChangeList;
        this.updateCommand = updateCommand;
        this.dtoClassInfo = dtoClassInfo;
        this.updateDtoList = updateDtoList;
    }

    public List<PropertyChangeEventModel> getUpdateEventList() {
        return onChangeList;
    }

    @Override
    public boolean skipListener(IListenerTiming eventTiming) {
        if(this.onChangeList == null || this.onChangeList.isEmpty()) {
            return true;
        }
        if(!this.matchExecuteTiming(eventTiming)) {
            return true;
        }
        if(!onChangeList.stream().filter(x -> eventTiming.equals(x.getExecuteTiming())
                || ExecuteTiming.BEFORE_UPDATE.equals(eventTiming))
                .anyMatch(x -> x.getUpdateTypes().stream().anyMatch(y -> updateCommand.getUpdateType().equals(y)))) {
            return true;
        }
        return false;
    }

    @Override
    public List<IListenerTiming> getExecuteTimingList() {
        return Arrays.asList(ExecuteTiming.BEFORE_UPDATE
                , ExecuteTiming.AFTER_UPDATE);
    }

    private List oldDtoList = null;
    private boolean initialized = false;

    private void initData() {
        oldDtoList = new ArrayList();
        Method keyFieldReadMethod = dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();
        for(int i = 0 ; i < updateDtoList.size(); i++) {
            Object updateDto = updateDtoList.get(i);
            if(dtoClassInfo.getDtoClass().isAssignableFrom(updateDto.getClass())) {
                Serializable key = (Serializable) FieldUtils.getFieldValue(updateDto, keyFieldReadMethod);
                if(key == null) {
                    oldDtoList.add(null);
                } else {
                    Object oldDto = dtoClassInfo.getDtoClassInfoHelper().getSelectService()
                            .getDtoById(dtoClassInfo.getEntityDtoServiceRelation(),key
                                    ,false, null);
                    oldDtoList.add(oldDto);
                }
            } else {
                oldDtoList.add(null);
            }
        }
    }

    @Override
    public void listenerExec(DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdateProcessor, IListenerTiming eventTiming) {
        if(!initialized) {
            initData();
            initialized = true;
        }
        List<PropertyChangeEventModel> execChangeList = onChangeList.stream().filter(x -> eventTiming.equals(x.getExecuteTiming()))
                .filter(x -> x.getUpdateTypes().stream().anyMatch(y -> updateCommand.getUpdateType().equals(y)))
                .collect(Collectors.toList());
        for (PropertyChangeEventModel propertyChangeEventModel : execChangeList) {
            for (int i = 0; i < updateDtoList.size(); i++) {
                Object newDto = updateDtoList.get(i);
                Object oldDto = oldDtoList.get(i);
                int compareResult = DtoClassInfo.equalProperties(dtoClassInfo
                        , newDto, oldDto, propertyChangeEventModel.getChangePropertyNames());
                boolean execFlag;
                if (propertyChangeEventModel.isTriggerOnAnyChanged()) {
                    execFlag = compareResult <= 0;
                } else {
                    execFlag = compareResult < 0;
                }
                if (execFlag) {
                    propertyChangeEventModel.getPropertyChangeEvent().exec(this.updateCommand.getUpdateType()
                            , dtoClassInfo, newDto, oldDto, propertyChangeEventModel.getUpdateParams());
                }
            }
        }
    }
}
