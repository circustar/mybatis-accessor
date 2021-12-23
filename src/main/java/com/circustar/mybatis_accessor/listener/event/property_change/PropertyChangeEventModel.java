package com.circustar.mybatis_accessor.listener.event.property_change;

import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.List;
import java.util.function.Supplier;

public class PropertyChangeEventModel {
    private String fromExpression;
    private String toExpression;
    private List<String> listenProperties;
    private IUpdateEvent updateEvent;
    private List<String> updateParams;
    private Class<? extends IUpdateEvent> updateEventClass;
    private Supplier<IUpdateEvent> supplier;
    private List<IUpdateCommand.UpdateType> updateTypes;
    private ExecuteTiming executeTiming;

    public PropertyChangeEventModel(
            String fromExpression, String toExpression
            , List<String> listenProperties
            , Class<? extends IUpdateEvent> updateEventClass
            , Supplier<IUpdateEvent> supplier
            , List<String> updateParams
            , List<IUpdateCommand.UpdateType> updateTypes
            , ExecuteTiming executeTiming
    ) {
        this.fromExpression = fromExpression;
        this.toExpression = toExpression;
        this.listenProperties = listenProperties;
        this.updateEventClass = updateEventClass;
        this.updateEvent = null;
        this.updateParams = updateParams;
        this.supplier = supplier;
        this.updateTypes = updateTypes;
        this.executeTiming = executeTiming;
    }

    public String getFromExpression() {
        return fromExpression;
    }

    public String getToExpression() {
        return toExpression;
    }

    public IUpdateEvent getUpdateEvent() {
        return updateEvent;
    }

    public List<String> getUpdateParams() {
        return updateParams;
    }

    public Class<? extends IUpdateEvent> getUpdateEventClass() {
        return updateEventClass;
    }

    public Supplier<IUpdateEvent> getSupplier() {
        return supplier;
    }

    public List<IUpdateCommand.UpdateType> getUpdateTypes() {
        return updateTypes;
    }

    public ExecuteTiming getExecuteTiming() {
        return executeTiming;
    }

    public List<String> getListenProperties() {
        return listenProperties;
    }
}
