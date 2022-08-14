package com.circustar.mybatis_accessor.listener;

import com.circustar.common_utils.listener.IListener;
import com.circustar.common_utils.listener.IListenerTiming;
import com.circustar.common_utils.parser.SPELParser;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;
import com.circustar.mybatis_accessor.listener.event.property_change.PropertyChangeEventModel;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.update_processor.AbstractDtoUpdateProcessor;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateProcessorPropertyChangeListener implements IListener<AbstractDtoUpdateProcessor> {
    private final List<PropertyChangeEventModel> onChangeList;
    private final IUpdateCommand updateCommand;
    private final DtoClassInfo dtoClassInfo;
    private final List updateDtoList;
    private List oldDtoList;
    private final AbstractDtoUpdateProcessor dtoUpdateProcessor;

    public UpdateProcessorPropertyChangeListener(AbstractDtoUpdateProcessor dtoUpdateProcessor) {
        this.dtoUpdateProcessor = dtoUpdateProcessor;
        this.dtoClassInfo = dtoUpdateProcessor.getDtoClassInfo();
        this.onChangeList = this.dtoClassInfo.getPropertyChangeEventList();
        this.updateCommand = dtoUpdateProcessor.getUpdateCommand();
        this.updateDtoList = dtoUpdateProcessor.getUpdateDtoList();
    }

    public List<PropertyChangeEventModel> getUpdateEventList() {
        return onChangeList;
    }

    @Override
    public void init() {
        if(this.onChangeList != null && !this.onChangeList.isEmpty()) {
            initData();
        }
    }

    @Override
    public boolean skipListener(IListenerTiming eventTiming) {
        if(this.onChangeList == null || this.onChangeList.isEmpty()) {
            return true;
        }
        return !onChangeList.stream().filter(x -> eventTiming.equals(x.getExecuteTiming()))
                .anyMatch(x -> x.getUpdateTypes().stream().anyMatch(y -> updateCommand.getUpdateType().equals(y)));
    }

    private void initData() {
        oldDtoList = new ArrayList();
        Method keyFieldReadMethod = dtoClassInfo.getKeyField().getPropertyDescriptor().getReadMethod();
        final ISelectService selectService = dtoClassInfo.getDtoClassInfoHelper().getSelectService();

        for(Object updateDto : updateDtoList) {
            Serializable key = this.dtoUpdateProcessor.getUpdateKey(updateDto);
            Object oldDto = null;
            if(key != null) {
                oldDto = selectService.getDtoById(dtoClassInfo.getEntityDtoServiceRelation(),key
                        ,false, null);
            }
            oldDtoList.add(oldDto);
        }
    }

    @Override
    public void listenerExec(AbstractDtoUpdateProcessor target
            , IListenerTiming eventTiming, String updateEventLogId, int level) throws MybatisAccessorException {
        List<PropertyChangeEventModel> execChangeList = onChangeList.stream().filter(x -> eventTiming.equals(x.getExecuteTiming()))
                .filter(x -> x.getUpdateTypes().stream().anyMatch(y -> updateCommand.getUpdateType().equals(y)))
                .collect(Collectors.toList());
        List executeDtoList = new ArrayList();
        for (PropertyChangeEventModel m : execChangeList) {
            executeDtoList.clear();
            for (int i = 0; i < updateDtoList.size(); i++) {
                Object newDto = updateDtoList.get(i);
                Object oldDto = oldDtoList.get(i);
                if(!CollectionUtils.isEmpty(m.getListenProperties())) {
                    Object compareNewObj;
                    Object compareOldObj;
                    if(IUpdateCommand.UpdateType.DELETE.equals(this.updateCommand.getUpdateType())) {
                        compareNewObj = oldDto;
                        compareOldObj = null;
                    } else {
                        compareNewObj = newDto;
                        compareOldObj = oldDto;
                    }
                    if(DtoClassInfo.equalPropertiesIgnoreEmpty(dtoClassInfo, compareNewObj, compareOldObj, m.getListenProperties()) > 0) {
                        continue;
                    }
                }

                if(!SPELParser.parseBooleanExpression(oldDto,m.getFromExpression(),true)) {
                    continue;
                }
                if(!SPELParser.parseBooleanExpression(newDto,m.getToExpression(),true)) {
                    continue;
                }
                executeDtoList.add(newDto);
            }
            if(!executeDtoList.isEmpty()) {
                m.getUpdateEvent().exec(m, this.updateCommand.getUpdateType(),
                        dtoClassInfo, executeDtoList, updateEventLogId);
            }
        }
    }
}
