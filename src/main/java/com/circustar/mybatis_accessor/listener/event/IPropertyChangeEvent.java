package com.circustar.mybatis_accessor.listener.event;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.List;

public interface IPropertyChangeEvent {
    ExecuteTiming getDefaultExecuteTiming();
    IUpdateCommand.UpdateType[] getDefaultUpdateTypes();
    void exec(IUpdateCommand.UpdateType updateType, DtoClassInfo dtoClassInfo
            , Object newDto, Object oldDto, List<String> params);
}
