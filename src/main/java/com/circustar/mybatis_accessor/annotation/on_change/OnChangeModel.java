package com.circustar.mybatis_accessor.annotation.on_change;

import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.HashMap;
import java.util.Map;

public class OnChangeModel {
    private String[] changePropertyNames;
    private boolean triggerOnAnyChanged;
    private IOnChangeExecutor onChangeExecutor;
    private String[] updateParams;

    public OnChangeModel(String[] changePropertyNames, boolean triggerOnAnyChanged
            , IOnChangeExecutor onChangeExecutor
            , String[] updateParams) {
        this.changePropertyNames = changePropertyNames;
        this.triggerOnAnyChanged = triggerOnAnyChanged;
        this.onChangeExecutor = onChangeExecutor;
        this.updateParams = updateParams;
    }

    public String[] getChangePropertyNames() {
        return changePropertyNames;
    }

    public boolean isTriggerOnAnyChanged() {
        return triggerOnAnyChanged;
    }

    public IOnChangeExecutor getOnChangeExecutor() {
        return onChangeExecutor;
    }

    public String[] getUpdateParams() {
        return updateParams;
    }

    private static Map<Class<? extends IOnChangeExecutor>, IOnChangeExecutor> onChangeExecutorMap = new HashMap<>();
    public static IOnChangeExecutor getInstance(Class<? extends IOnChangeExecutor> clazz) {
        try {
            if (!onChangeExecutorMap.containsKey(clazz)) {
                IOnChangeExecutor onChangeExecutor = clazz.newInstance();
                onChangeExecutorMap.put(clazz, onChangeExecutor);
                return onChangeExecutor;
            }
            return onChangeExecutorMap.get(clazz);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
