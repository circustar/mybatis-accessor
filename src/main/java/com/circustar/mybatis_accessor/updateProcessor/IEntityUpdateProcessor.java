package com.circustar.mybatis_accessor.updateProcessor;

import java.util.List;
import java.util.Map;

public interface IEntityUpdateProcessor<T> {
    boolean execUpdate();
    boolean execUpdate(Map<String, Object> options);
    List getUpdatedEntityList();
}
