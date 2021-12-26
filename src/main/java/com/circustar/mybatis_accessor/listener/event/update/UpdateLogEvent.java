package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class UpdateLogEvent implements IUpdateEvent<UpdateEventModel> {

    private static Logger logger= LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Override
    public ExecuteTiming getDefaultExecuteTiming() {
        return ExecuteTiming.BEFORE_UPDATE_START;
    }

    @Override
    public IUpdateCommand.UpdateType[] getDefaultUpdateTypes() {
        return new IUpdateCommand.UpdateType[] {IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE};
    }

    @Override
    public void exec(UpdateEventModel model, IUpdateCommand.UpdateType updateType
            , DtoClassInfo dtoClassInfo, List<Object> dtoList
            , String updateId, int level) {
        if(level == 0) {
            logger.info("UPDATE ID:" + updateId + ", TYPE:" + updateType.getName() + "" + dtoClassInfo.getDtoClass().getName());
            logger.info(dtoList);
        }
    }
}
