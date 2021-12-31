package com.circustar.common_utils.listener;

import java.util.List;

public interface IListenerContext<T> {
    void init(T target);
    T getListenTarget();
    List<IListener<T>> getListenerList();
    default boolean skipAllListener(final IListenerTiming eventTiming) {
        return !getListenerList().stream().filter(x -> !x.skipListener(eventTiming)).findAny().isPresent();
    }
    default void execListeners(final IListenerTiming eventTiming, final String updateEventLogId, final int level) {
        List<IListener<T>> listenerList = getListenerList();
        if(listenerList == null || listenerList.isEmpty()) {
            return;
        }
        T target = getListenTarget();
        for(IListener<T> listener : listenerList) {
            if(!listener.skipListener(eventTiming)) {
                listener.listenerExec(target, eventTiming, updateEventLogId, level);
            }
        }
    }
    default void dispose() {};
}
