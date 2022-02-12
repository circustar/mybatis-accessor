package com.circustar.mybatis_accessor.annotation.scan;

import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.mybatis_accessor.converter.DefaultConverter;
import com.circustar.mybatis_accessor.converter.IConverter;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DtoEntityRelations.class)
@Inherited
public @interface DtoEntityRelation {
    Class dtoClass() default Void.class;
    Class entityClass() default Void.class;
    String name() default "";
    Class<? extends IService> service() default IService.class;
    Class<? extends IConverter> convertDtoToEntityClazz() default DefaultConverter.class;
    Class<? extends IConverter> convertEntityToDtoClazz() default DefaultConverter.class;
}
