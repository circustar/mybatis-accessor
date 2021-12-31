package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class UpdateEventModel {
    private final String onExpression;
    private IUpdateEvent updateEvent;
    private final List<String> updateParams;
    private final Class<? extends IUpdateEvent> updateEventClass;
    private final Supplier<IUpdateEvent> supplier;
    private List<IUpdateCommand.UpdateType> updateTypes;
    private ExecuteTiming executeTiming;

    public UpdateEventModel(String onExpression
            , Class<? extends IUpdateEvent> updateEventClass
            , Supplier<IUpdateEvent> supplier
            , List<String> updateParams
            , List<IUpdateCommand.UpdateType> updateTypes
            , ExecuteTiming executeTiming
            ) {
        this.onExpression = onExpression;
        this.updateEventClass = updateEventClass;
        this.updateParams = updateParams;
        this.supplier = supplier;
        this.updateTypes = updateTypes;
        this.executeTiming = executeTiming;
    }

    public String getOnExpression() {
        return onExpression;
    }

    public IUpdateEvent getUpdateEvent() {
        if(updateEvent == null) {
            updateEvent = supplier.get();
        }
        return updateEvent;
    }

    public List<String> getUpdateParams() {
        return updateParams;
    }

    public Class<? extends IUpdateEvent> getUpdateEventClass() {
        return updateEventClass;
    }

    public List<IUpdateCommand.UpdateType> getUpdateTypes() {
        if(this.updateTypes == null || this.updateTypes.isEmpty()) {
            this.updateTypes = Arrays.asList(getUpdateEvent().getDefaultUpdateTypes());
        }
        return this.updateTypes;
    }

    public ExecuteTiming getExecuteTiming() {
        if(ExecuteTiming.DEFAULT.equals(this.executeTiming)) {
            this.executeTiming = getUpdateEvent().getDefaultExecuteTiming();
        }
        return this.executeTiming;
    }
}
