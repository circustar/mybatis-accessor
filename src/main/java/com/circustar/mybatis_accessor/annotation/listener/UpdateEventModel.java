package com.circustar.mybatis_accessor.annotation.listener;

import java.util.function.Supplier;

public class UpdateEventModel {
    private String onExpression;
    private IUpdateEvent afterUpdateEvent;
    private String[] updateParams;
    private Supplier<IUpdateEvent> supplier;

    public UpdateEventModel(String onExpression
            , Supplier<IUpdateEvent> supplier
            , String[] updateParams
            ) {
        this.onExpression = onExpression;
        this.afterUpdateEvent = null;
        this.updateParams = updateParams;
        this.supplier = supplier;
    }

    public String getOnExpression() {
        return onExpression;
    }

    public IUpdateEvent getUpdateEvent() {
        if(afterUpdateEvent == null) {
            afterUpdateEvent = supplier.get();
        }
        return afterUpdateEvent;
    }

    public String[] getUpdateParams() {
        return updateParams;
    }

}
