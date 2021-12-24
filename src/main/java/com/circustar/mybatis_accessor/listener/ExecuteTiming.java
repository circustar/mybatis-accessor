package com.circustar.mybatis_accessor.listener;

import com.circustar.common_utils.listener.IListenerTiming;

public enum ExecuteTiming implements IListenerTiming {
    BEFORE_UPDATE,
    AFTER_UPDATE,
    BEFORE_SUB_ENTITY_UPDATE,
    AFTER_SUB_ENTITY_UPDATE,
    DEFAULT;

    @Override
    public boolean match(IListenerTiming eventTiming) {
        return this.equals(eventTiming);
    }
}
