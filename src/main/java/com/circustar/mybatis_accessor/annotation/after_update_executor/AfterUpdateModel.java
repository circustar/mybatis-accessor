package com.circustar.mybatis_accessor.annotation.after_update_executor;

import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AfterUpdateModel {
    private String onExpression;
    private IAfterUpdateExecutor afterUpdateExecutor;
    private String[] updateParams;
    private IUpdateCommand.UpdateType[] updateTypes;

    public AfterUpdateModel(String onExpression, IAfterUpdateExecutor afterUpdateExecutor, String[] updateParams, IUpdateCommand.UpdateType[] updateTypes) {
        this.onExpression = onExpression;
        this.afterUpdateExecutor = afterUpdateExecutor;
        this.updateParams = updateParams;
        this.updateTypes = updateTypes;
    }

    public String getOnExpression() {
        return onExpression;
    }

    public IAfterUpdateExecutor getAfterUpdateExecutor() {
        return afterUpdateExecutor;
    }

    public String[] getUpdateParams() {
        return updateParams;
    }

    public IUpdateCommand.UpdateType[] getUpdateTypes() {
        return updateTypes;
    }

    private static Map<Class<? extends IAfterUpdateExecutor>, IAfterUpdateExecutor> afterUpdateExecutorMap = new HashMap<>();
    public static IAfterUpdateExecutor getInstance(Class<? extends IAfterUpdateExecutor> clazz) {
        try {
            if (!afterUpdateExecutorMap.containsKey(clazz)) {
                IAfterUpdateExecutor iAfterUpdateExecutor = clazz.newInstance();
                afterUpdateExecutorMap.put(clazz, iAfterUpdateExecutor);
                return iAfterUpdateExecutor;
            }
            return afterUpdateExecutorMap.get(clazz);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
