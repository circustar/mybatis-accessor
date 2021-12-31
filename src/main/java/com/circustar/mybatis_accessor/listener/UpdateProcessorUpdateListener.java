package com.circustar.mybatis_accessor.listener;

import com.circustar.common_utils.listener.IListener;
import com.circustar.common_utils.listener.IListenerTiming;
import com.circustar.common_utils.parser.SPELParser;
import com.circustar.mybatis_accessor.listener.event.update.UpdateEventModel;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.update_processor.DefaultEntityCollectionUpdateProcessor;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class UpdateProcessorUpdateListener implements IListener<DefaultEntityCollectionUpdateProcessor> {
    private List<UpdateEventModel> updateEventList;
    private IUpdateCommand updateCommand;
    private DtoClassInfo dtoClassInfo;
    private List updateDtoList;
    public UpdateProcessorUpdateListener(List<UpdateEventModel> updateEventList
            , IUpdateCommand updateCommand
            , DtoClassInfo dtoClassInfo
            , List updateDtoList) {
        this.updateEventList = updateEventList;
        this.updateCommand = updateCommand;
        this.dtoClassInfo = dtoClassInfo;
        this.updateDtoList = updateDtoList;
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
    public void listenerExec(DefaultEntityCollectionUpdateProcessor target
            , IListenerTiming eventTiming, String updateEventLogId, int level) {
        List<UpdateEventModel> updateModelList = updateEventList.stream()
                .filter(x -> eventTiming.equals(x.getExecuteTiming()))
                .filter(x -> x.getUpdateTypes().stream().anyMatch(y -> updateCommand.getUpdateType().equals(y)))
                .collect(Collectors.toList());
        List executeDtoList = new ArrayList();
        for(UpdateEventModel m : updateModelList) {
            executeDtoList.clear();
            for(Object updateDto : updateDtoList) {
                boolean execFlag = true;
                if(StringUtils.hasLength(m.getOnExpression())) {
                    execFlag = (boolean) SPELParser.parseExpression(updateDto,m.getOnExpression());
                }
                if(!execFlag) {
                    continue;
                }
                executeDtoList.add(updateDto);
            }
            if(!executeDtoList.isEmpty()) {
                m.getUpdateEvent().exec(m, this.updateCommand.getUpdateType(),
                        dtoClassInfo, executeDtoList, updateEventLogId, level);
            }
        }
    }
}
