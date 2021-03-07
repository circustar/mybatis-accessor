package com.circustar.mybatis_accessor.classInfo;

import java.util.*;

public enum CollectionType {
    collection(Collection.class, ArrayList.class),
    abstractCollection(AbstractCollection.class, ArrayList.class),
    list(List.class, ArrayList.class),
    abstractList(AbstractList.class, ArrayList.class),
    set(Set.class, HashSet.class),
    sortedSet(SortedSet.class, TreeSet.class),
    navigableSet(NavigableSet.class, TreeSet.class),
    abstractSet(AbstractSet.class, HashSet.class),
    queue(Queue.class, PriorityQueue.class),
    abstractQueue(AbstractQueue.class, PriorityQueue.class),
    deque(Deque.class, ArrayDeque.class),
    ;
    private Class<? extends Collection> inferfaceClass;
    private Class<? extends Collection> implementClass;
    CollectionType(Class inferfaceClass, Class implementClass) {
        this.inferfaceClass = inferfaceClass;
        this.implementClass = implementClass;
    }
    public Class<? extends Collection> getInterface() {
        return this.inferfaceClass;
    }
    public Class<? extends Collection> getImplementClass() {
        return this.implementClass;
    }
    public static Class<? extends Collection> getSupportCollectionType(Class t) {
        if(Collection.class.isAssignableFrom(t) && !t.isInterface()) {
            return t;
        }
        return  Arrays.stream(CollectionType.values()).filter(x -> x.getInterface() == t).map(x -> x.getImplementClass()).findFirst().orElse(null);
    }
}
