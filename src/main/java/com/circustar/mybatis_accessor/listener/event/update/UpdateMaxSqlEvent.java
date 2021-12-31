package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;

import java.util.List;

public class UpdateMaxSqlEvent extends UpdateSumSqlEvent implements IUpdateEvent<UpdateEventModel> {
    private static final String ORIGINAL_SQL = "select max(t1.%s) from %s t1 where t1.%s = %s.%s";

    @Override
    protected String getOriginalSql(List<String> originParams) {
        return UpdateMaxSqlEvent.ORIGINAL_SQL;
    }
}
