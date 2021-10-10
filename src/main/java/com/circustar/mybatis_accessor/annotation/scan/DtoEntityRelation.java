package com.circustar.mybatis_accessor.annotation.scan;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.provider.*;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DtoEntityRelations.class)
@Inherited
public @interface DtoEntityRelation {
    Class dtoClass() default Void.class;
    Class entityClass() default Void.class;
    Class<? extends IService> service() default IService.class;
    Class<? extends IUpdateProcessorProvider>[] updateObjectProviders() default {
    };
}
