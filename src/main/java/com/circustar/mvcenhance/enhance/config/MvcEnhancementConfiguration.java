package com.circustar.mvcenhance.enhance.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.circustar.mvcenhance.enhance.mybatisplus.injector.PhysicDeleteSqlInjector;
import com.circustar.mvcenhance.enhance.service.CrudService;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
public class MvcEnhancementConfiguration implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private EnhancedConversionService enhancedConversionService = null;
    private IEntityDtoServiceRelationMap entityDtoServiceRelationMap = null;
    private PhysicDeleteSqlInjector physicDeleteSqlInjector = null;
    private ConversionService conversionService = null;
    private ScanRelationOnStartup scanRelationOnStartup;
    private ICrudService crudService;

    @Bean
    @ConditionalOnProperty("mybatis-plus.global-config.db-config.logic-delete-field")
    public PhysicDeleteSqlInjector getPhysicDeleteSqlInjector() {
        if(this.physicDeleteSqlInjector == null) {
            this.physicDeleteSqlInjector = new PhysicDeleteSqlInjector();
        }
        return this.physicDeleteSqlInjector;
    }

    public ConversionService getConversionService(){
        if(this.conversionService == null) {
            this.conversionService = applicationContext.getBean(ConversionService.class);
        }
        return this.conversionService;
    }

    @Bean
    public EnhancedConversionService getEnhancedConversionService() {
        if(this.enhancedConversionService == null) {
            this.enhancedConversionService = new EnhancedConversionService(getConversionService());
        }
        return enhancedConversionService;
    }

    @Bean
    public IEntityDtoServiceRelationMap getEntityDtoServiceRelationMap() {
        if(this.entityDtoServiceRelationMap == null) {
            this.entityDtoServiceRelationMap = new EntityDtoServiceRelationMap();
        }
        return this.entityDtoServiceRelationMap;
    }

    @Bean
    public ScanRelationOnStartup getStartupService() {
        if(this.scanRelationOnStartup == null) {
            this.scanRelationOnStartup = new ScanRelationOnStartup(applicationContext, getEntityDtoServiceRelationMap());
        }
        return this.scanRelationOnStartup;
    }

    @Bean
    public ICrudService getCrudService() {
        if(this.crudService == null) {
            this.crudService = new CrudService(applicationContext, getEnhancedConversionService(), getEntityDtoServiceRelationMap());
        }
        return this.crudService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
