package com.circustar.mybatis_accessor.listener;

import com.circustar.common_utils.listener.IListener;
import com.circustar.common_utils.listener.IListenerTiming;
import com.circustar.common_utils.parser.SPELParser;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.listener.event.decode.DecodeEventModel;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import com.circustar.mybatis_accessor.update_processor.DefaultEntityCollectionUpdateProcessor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateProcessorDecodeListener implements IListener<DefaultEntityCollectionUpdateProcessor> {
    private List<DecodeEventModel> decodeEventModelList;
    private IUpdateCommand updateCommand;
    private DtoClassInfo dtoClassInfo;
    private List updateDtoList;
    public UpdateProcessorDecodeListener(List<DecodeEventModel> decodeEventModelList
            , IUpdateCommand updateCommand
            , DtoClassInfo dtoClassInfo
            , List updateDtoList) {
        this.decodeEventModelList = decodeEventModelList;
        this.updateCommand = updateCommand;
        this.dtoClassInfo = dtoClassInfo;
        this.updateDtoList = updateDtoList;
    }

    public List<DecodeEventModel> getDecodeEventModelList() {
        return decodeEventModelList;
    }

    @Override
    public boolean skipListener(IListenerTiming eventTiming) {
        if(this.decodeEventModelList == null || this.decodeEventModelList.isEmpty()) {
            return true;
        }
        if(!decodeEventModelList.stream().filter(x -> eventTiming.equals(x.getExecuteTiming()))
                .anyMatch(x -> x.getUpdateTypes().stream()
                        .anyMatch(y -> updateCommand.getUpdateType().equals(y)))) {
            return true;
        }
        return false;
    }

    @Override
    public void listenerExec(DefaultEntityCollectionUpdateProcessor target
            , IListenerTiming eventTiming, String updateEventLogId, int level) {
        List<DecodeEventModel> updateModelList = this.decodeEventModelList.stream()
                .filter(x -> eventTiming.equals(x.getExecuteTiming()))
                .filter(x -> x.getUpdateTypes().stream().anyMatch(y -> updateCommand.getUpdateType().equals(y)))
                .collect(Collectors.toList());
        List executeDtoList = new ArrayList();
        for(DecodeEventModel m : updateModelList) {
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
                m.getDefaultDecodeEvent().exec(m, this.updateCommand.getUpdateType(),
                        dtoClassInfo, executeDtoList, updateEventLogId, level);
            }
        }
    }
}
