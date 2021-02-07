package com.circustar.mvcenhance.provider;

import java.util.Map;

public interface IEntityUpdater<T> {
    boolean execUpdate() throws Exception;
    boolean execUpdate(Map<String, Object> options) throws Exception;
}
