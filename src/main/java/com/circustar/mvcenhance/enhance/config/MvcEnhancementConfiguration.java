package com.circustar.mvcenhance.enhance.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.circustar.mvcenhance.enhance.mybatisplus.injector.EnhanceSqlInjector;
import com.circustar.mvcenhance.enhance.service.CrudService;
import com.circustar.mvcenhance.enhance.service.ISelectService;
import com.circustar.mvcenhance.enhance.service.SelectService;
import com.circustar.mvcenhance.enhance.update.*;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.convert.ConversionService;
import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelationMap;
import com.circustar.mvcenhance.enhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.enhance.relation.ScanRelationOnStartup;
import com.circustar.mvcenhance.enhance.service.ICrudService;
import com.circustar.mvcenhance.enhance.utils.EnhancedConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfigureAfter({MybatisPlusAutoConfiguration.class})
@Configuration
public class MvcEnhancementConfiguration {
    private ApplicationContext applicationContext;
    private EnhancedConversionService enhancedConversionService = null;
    private IEntityDtoServiceRelationMap entityDtoServiceRelationMap = null;
    private EnhanceSqlInjector enhanceSqlInjector = null;
    private ConversionService conversionService = null;
    private ScanRelationOnStartup scanRelationOnStartup;
    private ICrudService crudService;
    private ISelectService selectService;

    public MvcEnhancementConfiguration(ApplicationContext applicationContext, ConversionService conversionService) {
        this.applicationContext = applicationContext;
        this.enhanceSqlInjector = new EnhanceSqlInjector();
        this.conversionService = conversionService;
        this.entityDtoServiceRelationMap = new EntityDtoServiceRelationMap();
        this.enhancedConversionService = new EnhancedConversionService(conversionService, this.entityDtoServiceRelationMap);
        this.crudService = new CrudService(this.applicationContext, this.enhancedConversionService, this.entityDtoServiceRelationMap);
        this.selectService = new SelectService(this.applicationContext, this.enhancedConversionService, this.entityDtoServiceRelationMap);
        this.scanRelationOnStartup = new ScanRelationOnStartup(this.applicationContext, this.entityDtoServiceRelationMap);
    }

    @Bean
//    @ConditionalOnProperty("mybatis-plus.global-config.db-config.logic-delete-field")
    public EnhanceSqlInjector getEnhanceSqlInjector() {
        return this.enhanceSqlInjector;
    }

    public ConversionService getConversionService(){
        return this.conversionService;
    }

    @Bean
    public EnhancedConversionService getEnhancedConversionService() {
        return enhancedConversionService;
    }

    @Bean
    public IEntityDtoServiceRelationMap getEntityDtoServiceRelationMap() {
        return this.entityDtoServiceRelationMap;
    }

    @Bean
    public ScanRelationOnStartup getStartupService() {
        return this.scanRelationOnStartup;
    }

    @Bean
    public ICrudService getCrudService() {
        return this.crudService;
    }

    @Bean
    public ISelectService getSelectService() {
        return this.selectService;
    }

    @Bean
    public DefaultDeleteEntityByIdProvider getDefaultDeleteEntityByIdProvider() {
        return new DefaultDeleteEntityByIdProvider();
    }

    @Bean
    public AutoDetectUpdateEntityProvider getAutoDetectUpdateEntityProvider() {
        return new AutoDetectUpdateEntityProvider();
    }

    @Bean
    public DefaultDeleteEntitiesByIdsProvider getDefaultDeleteEntitiesByIdsProvider() {
        return new DefaultDeleteEntitiesByIdsProvider();
    }

    @Bean
    public DefaultInertEntityEntityProvider getDefaultInertEntityProvider() {
        return new DefaultInertEntityEntityProvider();
    }

    @Bean
    public DefaultInertEntitiesEntityProvider getDefaultInertEntitiesProvider() {
        return new DefaultInertEntitiesEntityProvider();
    }

    @Bean
    public DefaultSaveUpdateDeleteEntitiesProvider getDefaultSaveUpdateDeleteEntitiesProvider() {
        return new DefaultSaveUpdateDeleteEntitiesProvider();
    }

    @Bean
    public DefaultUpdateEntityProvider getDefaultUpdateEntityProvider() {
        return new DefaultUpdateEntityProvider();
    }

    @Bean
    public DefaultUpdateSubEntitiesProvider getDefaultUpdateSubEntitiesProvider() {
        return new DefaultUpdateSubEntitiesProvider();
    }
}
