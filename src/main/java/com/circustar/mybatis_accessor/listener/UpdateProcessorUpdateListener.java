package com.circustar.mybatis_accessor.listener;

import com.circustar.common_utils.listener.IListener;
import com.circustar.common_utils.listener.IListenerTiming;
import com.circustar.common_utils.parser.SPELParser;
import com.circustar.mybatis_accessor.listener.event.update.UpdateEventModel;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.updateProcessor.DefaultEntityCollectionUpdateProcessor;
import com.circustar.mybatis_accessor.updateProcessor.IEntityUpdateProcessor;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class UpdateProcessorUpdateListener implements IListener<DefaultEntityCollectionUpdateProcessor> {
    private List<UpdateEventModel> updateEventList;
    private IUpdateCommand updateCommand;
    private DtoClassInfo dtoClassInfo;
    private List updateDtoList;
    private List updateEntityList;
    private Collection<IEntityUpdateProcessor> subUpdateEntities;
    public UpdateProcessorUpdateListener(List<UpdateEventModel> updateEventList
            , IUpdateCommand updateCommand
            , DtoClassInfo dtoClassInfo
            , List updateDtoList
            , List updateEntityList
            , Collection<IEntityUpdateProcessor> subUpdateEntities) {
        this.updateEventList = updateEventList;
        this.updateCommand = updateCommand;
        this.dtoClassInfo = dtoClassInfo;
        this.updateDtoList = updateDtoList;
        this.updateEntityList = updateEntityList;
        this.subUpdateEntities = subUpdateEntities;
    }

    public List<UpdateEventModel> getUpdateEventList() {
        return updateEventList;
    }

    @Override
    public boolean skipListener(IListenerTiming eventTiming) {
        if(this.updateEventList == null || this.updateEventList.isEmpty()) {
            return true;
        }
        if(!updateEventList.stream().filter(x -> eventTiming.equals(x.getExecuteTiming()))
                .anyMatch(x -> x.getUpdateTypes().stream()
                        .anyMatch(y -> updateCommand.getUpdateType().equals(y)))) {
            return true;
        }
        return false;
    }

    @Override
    public void listenerExec(DefaultEntityCollectionUpdateProcessor defaultEntityCollectionUpdateProcessor
            , IListenerTiming eventTiming, String updateEventLogId, int level) {
        List<UpdateEventModel> updateModelList = updateEventList.stream()
                .filter(x -> eventTiming.equals(x.getExecuteTiming()))
                .filter(x -> x.getUpdateTypes().stream().anyMatch(y -> updateCommand.getUpdateType().equals(y)))
                .collect(Collectors.toList());
        for(UpdateEventModel m : updateModelList) {
            List executeDtoList = new ArrayList();
            for(int i = 0 ; i < updateDtoList.size(); i++) {
                boolean execFlag = true;
                if(StringUtils.hasLength(m.getOnExpression())) {
                    execFlag = (boolean) SPELParser.parseExpression(updateDtoList.get(i),m.getOnExpression());
                }
                if(!execFlag) {
                    continue;
                }
                executeDtoList.add(updateDtoList.get(i));
            }
            if(!executeDtoList.isEmpty()) {
                m.getUpdateEvent().exec(m, this.updateCommand.getUpdateType(),
                        dtoClassInfo, executeDtoList, updateEventLogId, level);
            }
        }
    }
}
