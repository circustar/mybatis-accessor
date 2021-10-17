package com.circustar.mybatis_accessor.annotation.after_update;

import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.HashMap;
import java.util.Map;

public class AfterUpdateModel {
    private String onExpression;
    private IAfterUpdateExecutor afterUpdateExecutor;
    private String[] updateParams;

    public AfterUpdateModel(String onExpression, IAfterUpdateExecutor afterUpdateExecutor, String[] updateParams) {
        this.onExpression = onExpression;
        this.afterUpdateExecutor = afterUpdateExecutor;
        this.updateParams = updateParams;
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

    private static final Map<Class<? extends IAfterUpdateExecutor>, IAfterUpdateExecutor> afterUpdateExecutorMap = new HashMap<>();
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
