package com.circustar.mybatis_accessor.annotation.listener.summary;

import com.circustar.mybatis_accessor.annotation.listener.IUpdateEvent;
import com.circustar.mybatis_accessor.annotation.listener.UpdateEventModel;

import java.util.List;

public class UpdateAvgSqlEvent extends UpdateSumSqlEvent implements IUpdateEvent<UpdateEventModel> {
    private static final String precisionStr = "###precision###";
    private static final String originalSql = "select round(sum(t1.%s)/count(*), " + precisionStr + ") from %s t1 where t1.%s = %s.%s";

    @Override
    protected String getOriginalSql(List<String> originParams) {
        String precision = originParams.get(3);
        return UpdateAvgSqlEvent.originalSql.replace(UpdateAvgSqlEvent.precisionStr, precision);
    }
}
