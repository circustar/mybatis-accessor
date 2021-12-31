package com.circustar.common_utils.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CollectionUtils {
    public static boolean isCollection(final Object object) {
        if(object == null) {return false;}

        return Collection.class.isAssignableFrom(object.getClass());
    }

    public static List convertToList(final Object object) {
        if(object == null) {return Collections.emptyList();}

        if(Collection.class.isAssignableFrom(object.getClass())) {
            if(List.class.isAssignableFrom(object.getClass())) {
                return (List) object;
            }
            return new ArrayList((Collection)object);
        }
        return Collections.singletonList(object);
    }

    public static <T> String[] convertStreamToStringArray(final Stream<T> stream) {
        List<T> list = stream.collect(Collectors.toList());
        return list.toArray(new String[0]);
    }
}
