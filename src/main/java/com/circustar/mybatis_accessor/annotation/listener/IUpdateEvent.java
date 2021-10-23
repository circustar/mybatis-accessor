package com.circustar.mybatis_accessor.annotation.listener;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.List;

public interface IUpdateEvent<T> {
    default ExecuteTiming getExecuteTiming() {return ExecuteTiming.AFTER_SUB_ENTITY_UPDATE;}
    IUpdateCommand.UpdateType[] getUpdateTypes();
    void exec(T model, IUpdateCommand.UpdateType updateType
            , DtoClassInfo dtoClassInfo, List<Object> dtoList, List<Object> entityList);
}
