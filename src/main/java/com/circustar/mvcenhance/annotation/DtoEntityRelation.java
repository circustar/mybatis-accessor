package com.circustar.mvcenhance.annotation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.provider.*;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DtoEntityRelations.class)
@Inherited
public @interface DtoEntityRelation {
    Class dtoClass() default Void.class;
    Class entityClass() default Void.class;
    Class<? extends IService> service() default IService.class;
    Class<? extends IUpdateEntityProvider>[] updateObjectProviders() default {
    };
}
