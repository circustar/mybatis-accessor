package com.circustar.common_utils.listener;

import java.util.List;

public interface IListener<T> {
    List<IListenerTiming> getExecuteTimingList();
    void listenerExec(T t, IListenerTiming eventTiming);
    default boolean matchExecuteTiming(IListenerTiming executeTiming) {
        if(getExecuteTimingList() == null) {
            return true;
        }
        return this.getExecuteTimingList().stream().anyMatch(x -> executeTiming.match(x));
    }
    boolean skipListener(IListenerTiming eventTiming);
}
