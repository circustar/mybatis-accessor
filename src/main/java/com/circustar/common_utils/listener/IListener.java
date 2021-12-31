package com.circustar.common_utils.listener;

public interface IListener<T> {
    void listenerExec(T target, IListenerTiming eventTiming, String updateEventLogId, int level);
    boolean skipListener(IListenerTiming eventTiming);
}
