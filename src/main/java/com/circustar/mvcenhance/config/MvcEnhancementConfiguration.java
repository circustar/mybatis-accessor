package com.circustar.mvcenhance.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.circustar.mvcenhance.field.DtoClassInfoHelper;
import com.circustar.mvcenhance.field.EntityClassInfoHelper;
import com.circustar.mvcenhance.utils.TableInfoUtils;
import com.circustar.mvcenhance.injector.EnhanceSqlInjector;
import com.circustar.mvcenhance.service.CrudService;
import com.circustar.mvcenhance.service.ISelectService;
import com.circustar.mvcenhance.service.SelectService;
import com.circustar.mvcenhance.provider.*;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelationMap;
import com.circustar.mvcenhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.relation.ScanRelationOnStartup;
import com.circustar.mvcenhance.service.ICrudService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoConfigureAfter({MybatisPlusAutoConfiguration.class})
@Configuration
public class MvcEnhancementConfiguration {
    private ApplicationContext applicationContext;
    private IEntityDtoServiceRelationMap entityDtoServiceRelationMap = null;
    private EnhanceSqlInjector enhanceSqlInjector = null;
    private ScanRelationOnStartup scanRelationOnStartup;
    private ICrudService crudService;
    private ISelectService selectService;
    private EntityClassInfoHelper entityClassInfoHelper;
    private DtoClassInfoHelper dtoClassInfoHelper;

    public MvcEnhancementConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.enhanceSqlInjector = new EnhanceSqlInjector();
        this.entityDtoServiceRelationMap = new EntityDtoServiceRelationMap();
        this.entityClassInfoHelper = new EntityClassInfoHelper();
        this.dtoClassInfoHelper = new DtoClassInfoHelper(this.entityDtoServiceRelationMap, this.entityClassInfoHelper);
        this.crudService = new CrudService(this.applicationContext, this.dtoClassInfoHelper, this.entityDtoServiceRelationMap);
        this.selectService = new SelectService(this.applicationContext, this.entityDtoServiceRelationMap, this.dtoClassInfoHelper);
        this.scanRelationOnStartup = new ScanRelationOnStartup(this.applicationContext, this.entityDtoServiceRelationMap);

        TableInfoUtils.scanPackages.getAndSet(getMapperScanPackages(this.applicationContext));
    }

    private List<String> getMapperScanPackages(ApplicationContext applicationContext) {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(MapperScan.class);
        return Arrays.stream(beanNames).map(x -> applicationContext.findAnnotationOnBean(x, MapperScan.class))
                .map(x -> Stream.concat(Stream.of(x.value()), Stream.of(x.basePackages())))
                .flatMap(x -> x).collect(Collectors.toList());
    }

    @Bean
//    @ConditionalOnProperty("mybatis-plus.global-config.db-config.logic-delete-field")
    public EnhanceSqlInjector getEnhanceSqlInjector() {
        return this.enhanceSqlInjector;
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
    public DefaultDeleteEntitiesProvider getDefaultDeleteEntitiesProvider() {
        return DefaultDeleteEntitiesProvider.getInstance();
    }

    @Bean
    public DefaultInsertEntitiesProvider getDefaultInsertEntitiesEntityProvider() {
        return DefaultInsertEntitiesProvider.getInstance();
    }

    @Bean
    public DefaultUpdateEntityProvider getDefaultUpdateEntityProvider() {
        return DefaultUpdateEntityProvider.getInstance();
    }

    @Bean
    public DtoClassInfoHelper getDtoClassInfoHelper() {
        return this.dtoClassInfoHelper;
    }

    @Bean
    public EntityClassInfoHelper getEntityClassInfoHelper() {
        return this.entityClassInfoHelper;
    }
}
