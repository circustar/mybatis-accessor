package com.circustar.mybatis_accessor.class_info;

import java.lang.reflect.Modifier;
import java.util.*;

public enum CollectionType {
    COLLECTION(Collection.class, ArrayList.class),
    ABSTRACT_COLLECTION(AbstractCollection.class, ArrayList.class),
    LIST(List.class, ArrayList.class),
    ABSTRACT_LIST(AbstractList.class, ArrayList.class),
    SET(Set.class, HashSet.class),
    SORTED_SET(SortedSet.class, TreeSet.class),
    NAVIGABLE_SET(NavigableSet.class, TreeSet.class),
    ABSTRACT_SET(AbstractSet.class, HashSet.class),
    QUEUE(Queue.class, PriorityQueue.class),
    ABSTRACT_QUEUE(AbstractQueue.class, PriorityQueue.class),
    DEQUE(Deque.class, ArrayDeque.class)
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
    public static Class<? extends Collection> getSupportCollectionType(Class clazz) {
        if(Collection.class.isAssignableFrom(clazz) && !clazz.isInterface() && Modifier.isPublic(clazz.getModifiers())) {
            return clazz;
        }
        Class<? extends Collection> result = Arrays.stream(CollectionType.values()).filter(x -> x.getInterface() == clazz).map(x -> x.getImplementClass()).findFirst().orElse(null);
        if(result != null) {
            return result;
        }
        return ArrayList.class;
    }
}
