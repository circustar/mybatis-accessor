package com.circustar.mybatisAccessor.annotation;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.circustar.mybatisAccessor.config.MybatisAccessorConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(value = {MybatisAccessorConfiguration.class})
@ConditionalOnWebApplication
@ConditionalOnClass(MybatisPlusAutoConfiguration.class)
public @interface EnableMybatisAccessor {
    RelationScanPackages relationScan();
    boolean enableSpringValidation() default true;
}
