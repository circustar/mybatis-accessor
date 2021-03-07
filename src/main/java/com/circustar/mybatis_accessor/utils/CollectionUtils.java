package com.circustar.mybatis_accessor.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionUtils {
    public static boolean isCollection(Object o) {
        if(o == null) {return false;}

        return Collection.class.isAssignableFrom(o.getClass());
    }

    public static Collection convertToCollection(Object o) {
        if(o == null) {return Collections.emptyList();}

        if(Collection.class.isAssignableFrom(o.getClass())) {
            Collection value =  (Collection)o;
            return value;
        }

        return Collections.singletonList(o);
    }

    public static <T> String[] convertStreamToStringArray(Stream<T> stream) {
        List<T> list = stream.collect(Collectors.toList());
        return list.toArray(new String[list.size()]);
    }
}
