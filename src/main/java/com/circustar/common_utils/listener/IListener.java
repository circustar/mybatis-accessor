package com.circustar.common_utils.listener;

public interface IListener<T> {
    void listenerExec(T t, IListenerTiming eventTiming, String updateId, int level);
    boolean skipListener(IListenerTiming eventTiming);
}
