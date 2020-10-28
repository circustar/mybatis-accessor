package com.circustar.mvcenhance.enhance.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;
import com.circustar.mvcenhance.enhance.relation.RelationScanPackages;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(MvcEnhancementConfiguration.class)
@ConditionalOnWebApplication
@ConditionalOnClass(MybatisPlusAutoConfiguration.class)
public @interface EnableMvcEnhancement {
    RelationScanPackages relationScan();
}
