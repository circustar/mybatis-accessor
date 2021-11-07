package com.circustar.mybatis_accessor.listener.event.update_event;

import com.circustar.mybatis_accessor.listener.event.IUpdateEvent;
import com.circustar.mybatis_accessor.listener.event.UpdateEventModel;

import java.util.List;

public class UpdateMinSqlEvent extends UpdateSumSqlEvent implements IUpdateEvent<UpdateEventModel> {
    private static final String originalSql = "select min(t1.%s) from %s t1 where t1.%s = %s.%s";

    @Override
    protected String getOriginalSql(List<String> originParams) {
        return UpdateMinSqlEvent.originalSql;
    }
}
