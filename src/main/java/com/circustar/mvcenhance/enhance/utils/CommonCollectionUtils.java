package com.circustar.mvcenhance.enhance.utils;

import java.util.Collection;
import java.util.Collections;

public class CommonCollectionUtils {
    public static Collection convertToCollection(Object o) {
        if(o == null) {return null;}

        if(Collection.class.isAssignableFrom(o.getClass())) {
            return (Collection)o;
        }

        return Collections.singletonList(o);
    }
}
