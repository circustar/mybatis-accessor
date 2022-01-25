package com.circustar.mybatis_accessor.update_processor;

import com.circustar.mybatis_accessor.common.MybatisAccessorException;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface IEntityUpdateProcessor<T> {
    boolean execUpdate(String updateEventLogId) throws MybatisAccessorException;
    boolean execUpdate(Map<String, Object> options, List<Supplier<Integer>> consumerList, String updateEventLogId, int level) throws MybatisAccessorException;
    List getUpdatedEntityList();
}
