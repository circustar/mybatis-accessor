package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.List;

public class UpdateLogEvent implements IUpdateEvent<UpdateEventModel> {

    private final static Logger LOGGER = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Override
    public ExecuteTiming getDefaultExecuteTiming() {
        return ExecuteTiming.BEFORE_ENTITY_UPDATE;
    }

    @Override
    public IUpdateCommand.UpdateType[] getDefaultUpdateTypes() {
        return new IUpdateCommand.UpdateType[] {IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE};
    }

    @Override
    public void exec(UpdateEventModel model, IUpdateCommand.UpdateType updateType
            , DtoClassInfo dtoClassInfo, List<Object> dtoList
            , String updateEventLogId) {
        LOGGER.info("UPDATE LOG EVENT, ID:" + updateEventLogId + ", TYPE:" + updateType.getName() + ", CLASS:" + dtoClassInfo.getDtoClass().getName());
        if(dtoList == null) {
            return;
        }
        for (Object obj : dtoList) {
            LOGGER.info("    DATA:" + obj.toString());
        }
    }
}
