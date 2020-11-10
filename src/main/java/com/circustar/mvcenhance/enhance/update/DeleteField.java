package com.circustar.mvcenhance.enhance.update;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@Inherited
public @interface DeleteField {
    String value();
}
