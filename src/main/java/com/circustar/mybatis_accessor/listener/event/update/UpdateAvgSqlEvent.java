package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;

import java.util.List;

public class UpdateAvgSqlEvent extends UpdateSumSqlEvent implements IUpdateEvent<UpdateEventModel> {
    private static final String PRECISION_STR = "###precision###";
    private static final String ORIGINAL_SQL = "select round(sum(t1.%s)/count(*), " + PRECISION_STR + ") from %s t1 where t1.%s = %s.%s";

    @Override
    protected String getOriginalSql(List<String> originParams) {
        String precision = originParams.get(3);
        return UpdateAvgSqlEvent.ORIGINAL_SQL.replace(UpdateAvgSqlEvent.PRECISION_STR, precision);
    }
}
