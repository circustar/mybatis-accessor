package com.circustar.mvcenhance.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.circustar.mvcenhance.classInfo.DtoClassInfoHelper;
import com.circustar.mvcenhance.classInfo.EntityClassInfoHelper;
import com.circustar.mvcenhance.relation.ScanValidatorOnStartup;
import com.circustar.mvcenhance.support.ControllerSupport;
import com.circustar.mvcenhance.utils.TableInfoUtils;
import com.circustar.mvcenhance.injector.EnhanceSqlInjector;
import com.circustar.mvcenhance.service.UpdateService;
import com.circustar.mvcenhance.service.ISelectService;
import com.circustar.mvcenhance.service.SelectService;
import com.circustar.mvcenhance.provider.*;
import com.circustar.mvcenhance.validator.DefaultDeleteValidator;
import com.circustar.mvcenhance.validator.DefaultInsertValidator;
import com.circustar.mvcenhance.validator.DefaultUpdateValidator;
import com.circustar.mvcenhance.validator.DtoValidatorManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import com.circustar.mvcenhance.relation.EntityDtoServiceRelationMap;
import com.circustar.mvcenhance.relation.IEntityDtoServiceRelationMap;
import com.circustar.mvcenhance.relation.ScanRelationOnStartup;
import com.circustar.mvcenhance.service.IUpdateService;
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
    private IUpdateService crudService;
    private ISelectService selectService;
    private EntityClassInfoHelper entityClassInfoHelper;
    private DtoClassInfoHelper dtoClassInfoHelper;
    private ControllerSupport controllerSupport;
    private DtoValidatorManager dtoValidatorManager;
    private ScanValidatorOnStartup scanValidatorOnStartup;

    public MvcEnhancementConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.enhanceSqlInjector = new EnhanceSqlInjector();
        this.entityDtoServiceRelationMap = new EntityDtoServiceRelationMap();
        this.entityClassInfoHelper = new EntityClassInfoHelper();
        this.dtoClassInfoHelper = new DtoClassInfoHelper(this.entityDtoServiceRelationMap, this.entityClassInfoHelper);
        this.crudService = new UpdateService(this.applicationContext, this.dtoClassInfoHelper, this.entityDtoServiceRelationMap);
        this.selectService = new SelectService(this.applicationContext, this.entityDtoServiceRelationMap, this.dtoClassInfoHelper);
        this.scanRelationOnStartup = new ScanRelationOnStartup(this.applicationContext, this.entityDtoServiceRelationMap);
        this.controllerSupport = new ControllerSupport();
        TableInfoUtils.scanPackages.getAndSet(getMapperScanPackages(this.applicationContext));
        this.dtoValidatorManager = new DtoValidatorManager(this.applicationContext
                , DefaultInsertEntityProvider.getInstance()
                , DefaultUpdateEntityProvider.getInstance()
                , DefaultDeleteEntityProvider.getInstance());
        this.scanValidatorOnStartup = new ScanValidatorOnStartup(this.dtoValidatorManager);
    }

    private List<String> getMapperScanPackages(ApplicationContext applicationContext) {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(MapperScan.class);
        return Arrays.stream(beanNames).map(x -> applicationContext.findAnnotationOnBean(x, MapperScan.class))
                .map(x -> Stream.concat(Stream.of(x.value()), Stream.of(x.basePackages())))
                .flatMap(x -> x).collect(Collectors.toList());
    }

    @Bean
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
    public IUpdateService getCrudService() {
        return this.crudService;
    }

    @Bean
    public ISelectService getSelectService() {
        return this.selectService;
    }

    @Bean
    public DefaultDeleteEntityProvider getDefaultDeleteEntitiesProvider() {
        return DefaultDeleteEntityProvider.getInstance();
    }

    @Bean
    public DefaultInsertEntityProvider getDefaultInsertEntitiesEntityProvider() {
        return DefaultInsertEntityProvider.getInstance();
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

    @Bean
    public ControllerSupport getControllerSupport() {
        return this.controllerSupport;
    }

    @Bean
    public DtoValidatorManager getDtoValidatorManager() {
        return this.dtoValidatorManager;
    }

    @Bean
    public ScanValidatorOnStartup getScanValidatorOnStartup() {
        return this.scanValidatorOnStartup;
    }
}
