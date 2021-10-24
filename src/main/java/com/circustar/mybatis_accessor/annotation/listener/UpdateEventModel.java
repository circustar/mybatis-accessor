package com.circustar.mybatis_accessor.annotation.listener;

import java.util.function.Supplier;

public class UpdateEventModel {
    private String onExpression;
    private IUpdateEvent updateEvent;
    private String[] updateParams;
    private Class<? extends IUpdateEvent> updateEventClass;
    private Supplier<IUpdateEvent> supplier;

    public UpdateEventModel(String onExpression
            , Class<? extends IUpdateEvent> updateEventClass
            , Supplier<IUpdateEvent> supplier
            , String[] updateParams
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

    public String[] getUpdateParams() {
        return updateParams;
    }

    public Class<? extends IUpdateEvent> getUpdateEventClass() {
        return updateEventClass;
    }
}
