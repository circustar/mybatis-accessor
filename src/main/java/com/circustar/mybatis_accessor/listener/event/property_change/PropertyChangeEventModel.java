package com.circustar.mybatis_accessor.listener.event.property_change;

import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class PropertyChangeEventModel {
    private final List<String> listenProperties;
    private final String fromExpression;
    private final String toExpression;
    private IUpdateEvent updateEvent;
    private final List<String> updateParams;
    private final Class<? extends IUpdateEvent> updateEventClass;
    private final Supplier<IUpdateEvent> supplier;
    private List<IUpdateCommand.UpdateType> updateTypes;
    private final ExecuteTiming executeTiming;

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

    public Supplier<IUpdateEvent> getSupplier() {
        return supplier;
    }

    public List<IUpdateCommand.UpdateType> getUpdateTypes() {
        if(this.updateTypes == null || this.updateTypes.isEmpty()) {
            this.updateTypes = Arrays.asList(getUpdateEvent().getDefaultUpdateTypes());
        }
        return this.updateTypes;
    }

    public ExecuteTiming getExecuteTiming() {
        if(ExecuteTiming.DEFAULT.equals(this.executeTiming)) {
            return getUpdateEvent().getDefaultExecuteTiming();
        } else {
            return this.executeTiming;
        }
    }

    public List<String> getListenProperties() {
        return listenProperties;
    }
}
