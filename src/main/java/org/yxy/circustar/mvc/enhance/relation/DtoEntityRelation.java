package org.yxy.circustar.mvc.enhance.relation;

import com.baomidou.mybatisplus.extension.service.IService;
import org.yxy.circustar.mvc.enhance.update.IUpdateObjectProvider;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DtpEntityRelations.class)
@Inherited
public @interface DtoEntityRelation {
    Class dtoClass() default Void.class;
    Class entityClass() default Void.class;
    Class<? extends IService> service() default IService.class;
    Class<IUpdateObjectProvider> converter() default  IUpdateObjectProvider.class;
}
