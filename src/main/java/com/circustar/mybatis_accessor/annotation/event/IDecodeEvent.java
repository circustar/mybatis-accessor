package com.circustar.mybatis_accessor.annotation.event;

import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

public interface IDecodeEvent<T> extends IUpdateEvent<T> {
    @Override
    default ExecuteTiming getDefaultExecuteTiming() {return ExecuteTiming.BEFORE_ENTITY_UPDATE;};

    @Override
    default IUpdateCommand.UpdateType[] getDefaultUpdateTypes() {
        return new IUpdateCommand.UpdateType[] {
                IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE
        };
    }
}
