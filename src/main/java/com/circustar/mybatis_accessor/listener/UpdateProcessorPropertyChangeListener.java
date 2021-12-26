package com.circustar.mybatis_accessor.listener;

import com.circustar.common_utils.collection.CollectionUtils;
import com.circustar.common_utils.listener.IListener;
import com.circustar.common_utils.listener.IListenerTiming;
import com.circustar.common_utils.parser.SPELParser;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.listener.event.property_change.PropertyChangeEventModel;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
        if(!onChangeList.stream().filter(x -> eventTiming.equals(x.getExecuteTiming())
                || ExecuteTiming.BEFORE_ENTITY_UPDATE.equals(eventTiming))
                .anyMatch(x -> x.getUpdateTypes().stream().anyMatch(y -> updateCommand.getUpdateType().equals(y)))) {
            return true;
        }
        return false;
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
    public void listenerExec(DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdateProcessor
            , IListenerTiming eventTiming, String updateId, int level) {
        if(!initialized) {
            initData();
            initialized = true;
        }
        List<PropertyChangeEventModel> execChangeList = onChangeList.stream().filter(x -> eventTiming.equals(x.getExecuteTiming()))
                .filter(x -> x.getUpdateTypes().stream().anyMatch(y -> updateCommand.getUpdateType().equals(y)))
                .collect(Collectors.toList());
        for (PropertyChangeEventModel m : execChangeList) {
            List executeDtoList = new ArrayList();
            for (int i = 0; i < updateDtoList.size(); i++) {
                Object newDto = updateDtoList.get(i);
                Object oldDto = oldDtoList.get(i);
                if(!CollectionUtils.isEmpty(m.getListenProperties())) {
                    if(DtoClassInfo.equalPropertiesIgnoreEmpty(dtoClassInfo, newDto, oldDto, m.getListenProperties()) > 0) {
                        continue;
                    }
                }

                if(StringUtils.hasLength(m.getFromExpression())) {
                    if(!(boolean) SPELParser.parseExpression(oldDto,m.getFromExpression())) {
                        continue;
                    }
                }
                if(StringUtils.hasLength(m.getToExpression())) {
                    if(!(boolean) SPELParser.parseExpression(oldDto,m.getToExpression())) {
                        continue;
                    }
                }
                executeDtoList.add(newDto);
            }
            if(!executeDtoList.isEmpty()) {
                m.getUpdateEvent().exec(m, this.updateCommand.getUpdateType(),
                        dtoClassInfo, executeDtoList, updateId, level);
            }
        }
    }
}
