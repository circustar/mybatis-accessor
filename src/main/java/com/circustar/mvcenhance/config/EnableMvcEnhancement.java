package com.circustar.mvcenhance.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;
import com.circustar.mvcenhance.annotation.RelationScanPackages;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(value = {MvcEnhancementConfiguration.class})
@ConditionalOnWebApplication
@ConditionalOnClass(MybatisPlusAutoConfiguration.class)
public @interface EnableMvcEnhancement {
    RelationScanPackages relationScan();
}
