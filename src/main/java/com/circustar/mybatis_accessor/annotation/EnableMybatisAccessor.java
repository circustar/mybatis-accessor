package com.circustar.mybatis_accessor.annotation;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.circustar.mybatis_accessor.config.MybatisAccessorConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(value = {MybatisAccessorConfiguration.class})
@ConditionalOnClass(MybatisPlusAutoConfiguration.class)
public @interface EnableMybatisAccessor {
    RelationScanPackages relationScan();
    boolean enableSpringValidation() default true;
}
