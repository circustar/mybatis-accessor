package com.circustar.mybatis_accessor.updateProcessor;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface IEntityUpdateProcessor<T> {
    boolean execUpdate();
    boolean execUpdate(Map<String, Object> options, List<Supplier<Integer>> consumerList, int level);
    List getUpdatedEntityList();
}
