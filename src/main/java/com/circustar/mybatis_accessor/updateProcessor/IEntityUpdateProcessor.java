package com.circustar.mybatis_accessor.updateProcessor;

import java.util.Map;

public interface IEntityUpdateProcessor<T> {
    boolean execUpdate() throws Exception;
    boolean execUpdate(Map<String, Object> options) throws Exception;
}
