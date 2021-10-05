package com.circustar.common_utils.collection;

import java.util.ArrayList;
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

    public static List convertToList(Object o) {
        if(o == null) {return Collections.emptyList();}

        if(Collection.class.isAssignableFrom(o.getClass())) {
            if(List.class.isAssignableFrom(o.getClass())) {
                return (List) o;
            }
            return new ArrayList((Collection)o);
        }
        return Collections.singletonList(o);
    }

    public static <T> String[] convertStreamToStringArray(Stream<T> stream) {
        List<T> list = stream.collect(Collectors.toList());
        return list.toArray(new String[0]);
    }
}
