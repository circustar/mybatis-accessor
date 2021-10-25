package com.circustar.mybatis_accessor.annotation.listener;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class UpdateEventModel {
    private String onExpression;
    private IUpdateEvent updateEvent;
    private List<String> updateParams;
    private Class<? extends IUpdateEvent> updateEventClass;
    private Supplier<IUpdateEvent> supplier;

    public UpdateEventModel(String onExpression
            , Class<? extends IUpdateEvent> updateEventClass
            , Supplier<IUpdateEvent> supplier
            , List<String> updateParams
            ) {
        this.onExpression = onExpression;
        this.updateEventClass = updateEventClass;
        this.updateEvent = null;
        this.updateParams = updateParams;
        this.supplier = supplier;
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
}
