package com.circustar.mybatis_accessor.annotation.listener.property_change;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class PropertyChangeEventModel {
    private List<String> changePropertyNames;
    private boolean triggerOnAnyChanged;
    private IPropertyChangeEvent propertyChangeEvent;
    private List<String> updateParams;
    private Supplier<IPropertyChangeEvent> supplier;

    public PropertyChangeEventModel(String[] changePropertyNames, boolean triggerOnAnyChanged
            , Supplier<IPropertyChangeEvent> supplier
            , String[] updateParams) {
        this.changePropertyNames = Arrays.asList(changePropertyNames);
        this.triggerOnAnyChanged = triggerOnAnyChanged;
        this.updateParams = Arrays.asList(updateParams);
        this.supplier = supplier;
    }

    public List<String> getChangePropertyNames() {
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

    public List<String> getUpdateParams() {
        return updateParams;
    }
}
