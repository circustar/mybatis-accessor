package com.circustar.mybatis_accessor.annotation.after_update;

public class AfterUpdateAvgExecutor extends AfterUpdateSumExecutor implements  IAfterUpdateExecutor {
    private static final String originalSql = "select sum(t1.%s)/count(*) from %s t1 where t1.%s = %s.%s";

    @Override
    protected String getOriginalSql() {
        return AfterUpdateAvgExecutor.originalSql;
    }
}
