package com.circustar.mybatis_accessor.annotation.event;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.List;

public interface IUpdateEvent<T> {
    ExecuteTiming getDefaultExecuteTiming();
    IUpdateCommand.UpdateType[] getDefaultUpdateTypes();
    void exec(T model, IUpdateCommand.UpdateType updateType
            , DtoClassInfo dtoClassInfo, List<Object> dtoList
            , String updateEventLogId, int level);
}
