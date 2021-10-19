package com.circustar.mybatis_accessor.annotation.listener.property_change;

import java.util.function.Supplier;

public class PropertyChangeEventModel {
    private String[] changePropertyNames;
    private boolean triggerOnAnyChanged;
    private IPropertyChangeEvent propertyChangeEvent;
    private String[] updateParams;
    private Supplier<IPropertyChangeEvent> supplier;

    public PropertyChangeEventModel(String[] changePropertyNames, boolean triggerOnAnyChanged
            , Supplier<IPropertyChangeEvent> supplier
            , String[] updateParams) {
        this.changePropertyNames = changePropertyNames;
        this.triggerOnAnyChanged = triggerOnAnyChanged;
        this.updateParams = updateParams;
        this.supplier = supplier;
    }

    public String[] getChangePropertyNames() {
        return changePropertyNames;
    }

    public boolean isTriggerOnAnyChanged() {
        return triggerOnAnyChanged;
    }

    public IPropertyChangeEvent getPropertyChangeEvent() {
        if(propertyChangeEvent == null) {
            propertyChangeEvent = supplier.get();
        }
        return propertyChangeEvent;
    }

    public String[] getUpdateParams() {
        return updateParams;
    }
}
