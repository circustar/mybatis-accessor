package com.circustar.mvcenhance.enhance.relation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mvcenhance.enhance.update.*;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DtpEntityRelations.class)
@Inherited
public @interface DtoEntityRelation {
    Class dtoClass() default Void.class;
    Class entityClass() default Void.class;
    Class<? extends IService> service() default IService.class;
    Class<? extends IUpdateEntityProvider>[] updateObjectProviders() default {
            AutoDetectUpdateEntityProvider.class, DefaultDeleteEntitiesByIdsProvider.class
        , DefaultDeleteEntityByIdProvider.class, DefaultInertEntitiesEntityProvider.class
        , DefaultInertEntityEntityProvider.class, DefaultUpdateEntityProvider.class
        , DefaultSaveUpdateDeleteEntitiesProvider.class, DefaultUpdateSubEntitiesProvider.class
    };
}
