package com.circustar.mybatis_accessor.listener;

import com.circustar.common_utils.listener.IListener;
import com.circustar.common_utils.listener.IListenerTiming;
import com.circustar.common_utils.parser.SPELParser;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;
import com.circustar.mybatis_accessor.listener.event.update.UpdateEventModel;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.update_processor.AbstractDtoUpdateProcessor;

import java.util.*;
import java.util.stream.Collectors;

public class UpdateProcessorUpdateListener implements IListener<AbstractDtoUpdateProcessor> {
    private final List<UpdateEventModel> updateEventList;
    private final IUpdateCommand updateCommand;
    private final DtoClassInfo dtoClassInfo;
    private final List updateDtoList;
    public UpdateProcessorUpdateListener(AbstractDtoUpdateProcessor dtoUpdateProcessor) {
        this.updateEventList = dtoUpdateProcessor.getDtoClassInfo().getUpdateEventList();
        this.updateCommand = dtoUpdateProcessor.getUpdateCommand();
        this.dtoClassInfo = dtoUpdateProcessor.getDtoClassInfo();
        this.updateDtoList = dtoUpdateProcessor.getUpdateDtoList();
    }

    public List<UpdateEventModel> getUpdateEventList() {
        return updateEventList;
    }

    @Override
    public boolean skipListener(IListenerTiming eventTiming) {
        if(this.updateEventList == null || this.updateEventList.isEmpty()) {
            return true;
        }
        return !updateEventList.stream().filter(x -> eventTiming.equals(x.getExecuteTiming()))
                .anyMatch(x -> x.getUpdateTypes().stream()
                        .anyMatch(y -> updateCommand.getUpdateType().equals(y)));
    }

    @Override
    public void listenerExec(AbstractDtoUpdateProcessor target
            , IListenerTiming eventTiming, String updateEventLogId, int level) throws MybatisAccessorException {
        List<UpdateEventModel> updateModelList = updateEventList.stream()
                .filter(x -> eventTiming.equals(x.getExecuteTiming()))
                .filter(x -> x.getUpdateTypes().stream().anyMatch(y -> updateCommand.getUpdateType().equals(y)))
                .collect(Collectors.toList());
        List executeDtoList = new ArrayList();
        for(UpdateEventModel m : updateModelList) {
            executeDtoList.clear();
            for(Object updateDto : updateDtoList) {
                if(!SPELParser.parseBooleanExpression(updateDto,m.getOnExpression(),true)) {
                    continue;
                }
                executeDtoList.add(updateDto);
            }
            if(!executeDtoList.isEmpty()) {
                m.getUpdateEvent().exec(m, this.updateCommand.getUpdateType(),
                        dtoClassInfo, executeDtoList, updateEventLogId);
            }
        }
    }
}
