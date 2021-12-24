package com.circustar.common_utils.listener;

public interface IListener<T> {
    void listenerExec(T t, IListenerTiming eventTiming);
    boolean skipListener(IListenerTiming eventTiming);
}
