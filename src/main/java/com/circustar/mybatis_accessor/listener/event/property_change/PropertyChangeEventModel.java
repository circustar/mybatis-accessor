package com.circustar.mybatis_accessor.listener.event.property_change;

import com.circustar.mybatis_accessor.annotation.event.IPropertyChangeEvent;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class PropertyChangeEventModel {
    private List<String> changePropertyNames;
    private boolean triggerOnAnyChanged;
    private IPropertyChangeEvent propertyChangeEvent;
    private List<String> updateParams;
    private Supplier<IPropertyChangeEvent> supplier;
    private List<IUpdateCommand.UpdateType> updateTypes;
    private ExecuteTiming executeTiming;

    public PropertyChangeEventModel(String[] changePropertyNames, boolean triggerOnAnyChanged
            , Supplier<IPropertyChangeEvent> supplier
            , String[] updateParams
            , List<IUpdateCommand.UpdateType> updateTypes
            , ExecuteTiming executeTiming) {
        this.changePropertyNames = Arrays.asList(changePropertyNames);
        this.triggerOnAnyChanged = triggerOnAnyChanged;
        this.updateParams = Arrays.asList(updateParams);
        this.supplier = supplier;
        this.updateTypes = updateTypes;
        this.executeTiming = executeTiming;
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

    public List<IUpdateCommand.UpdateType> getUpdateTypes() {
        if(this.updateTypes == null || this.updateTypes.isEmpty()) {
            this.updateTypes = Arrays.asList(getPropertyChangeEvent().getDefaultUpdateTypes());
        }
        return this.updateTypes;
    }

    public ExecuteTiming getExecuteTiming() {
        if(ExecuteTiming.NONE.equals(this.executeTiming)) {
            this.executeTiming = getPropertyChangeEvent().getDefaultExecuteTiming();
        }
        return this.executeTiming;
    }
}
