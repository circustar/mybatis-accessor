package com.circustar.common_utils.listener;

import com.circustar.mybatis_accessor.common.MybatisAccessorException;

public interface IListener<T> {
    void listenerExec(T target, IListenerTiming eventTiming, String updateEventLogId, int level) throws MybatisAccessorException;
    boolean skipListener(IListenerTiming eventTiming);
    default void init() {};
}
