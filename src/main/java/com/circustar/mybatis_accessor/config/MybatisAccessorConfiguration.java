package com.circustar.mybatis_accessor.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.EntityClassInfoHelper;
import com.circustar.mybatis_accessor.converter.DefaultConverter;
import com.circustar.mybatis_accessor.listener.event.update.*;
import com.circustar.mybatis_accessor.provider.DefaultDeleteByIdProcessorProvider;
import com.circustar.mybatis_accessor.provider.DefaultDeleteProcessorProvider;
import com.circustar.mybatis_accessor.provider.DefaultInsertProcessorProvider;
import com.circustar.mybatis_accessor.provider.DefaultUpdateProcessorProvider;
import com.circustar.mybatis_accessor.support.MybatisAccessorService;
import com.circustar.mybatis_accessor.injector.EnhanceSqlInjector;
import com.circustar.mybatis_accessor.service.UpdateService;
import com.circustar.mybatis_accessor.service.ISelectService;
import com.circustar.mybatis_accessor.service.SelectService;
import com.circustar.mybatis_accessor.support.MybatisAccessorUpdateManager;
import com.circustar.mybatis_accessor.utils.TableInfoUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import com.circustar.mybatis_accessor.relation.EntityDtoServiceRelationMap;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import com.circustar.mybatis_accessor.relation.ScanRelationOnStartup;
import com.circustar.mybatis_accessor.service.IUpdateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoConfigureAfter({MybatisPlusAutoConfiguration.class})
@Configuration
public class MybatisAccessorConfiguration {
    private ApplicationContext applicationContext;
    private IEntityDtoServiceRelationMap entityDtoServiceRelationMap = null;
    private EnhanceSqlInjector enhanceSqlInjector;
    private ScanRelationOnStartup scanRelationOnStartup;
    private IUpdateService updateService;
    private ISelectService selectService;
    private EntityClassInfoHelper entityClassInfoHelper;
    private DtoClassInfoHelper dtoClassInfoHelper;
    private MybatisAccessorService mybatisAccessorService;
    private MybatisAccessorUpdateManager updateManager;
    private DefaultDeleteByIdProcessorProvider defaultDeleteByIdProcessorProvider;
    private DefaultDeleteProcessorProvider defaultDeleteProcessorProvider;
    private DefaultInsertProcessorProvider defaultInsertProcessorProvider;
    private DefaultUpdateProcessorProvider defaultUpdateProcessorProvider;
    private DefaultConverter defaultConverter;

    private UpdateAssignEvent updateAssignEvent;
    private UpdateAssignSqlEvent updateAssignSqlEvent;
    private UpdateAvgAssignEvent updateAvgAssignEvent;
    private UpdateAvgAssignSqlEvent updateAvgAssignSqlEvent;
    private UpdateAvgEvent updateAvgEvent;
    private UpdateAvgSqlEvent updateAvgSqlEvent;
    private UpdateCountEvent updateCountEvent;
    private UpdateCountSqlEvent updateCountSqlEvent;
    private UpdateExecuteBeanMethodEvent updateExecuteBeanMethodEvent;
    private UpdateExecuteSqlEvent updateExecuteSqlEvent;
    private UpdateFillEvent updateFillEvent;
    private UpdateMaxEvent updateMaxEvent;
    private UpdateMaxSqlEvent updateMaxSqlEvent;
    private UpdateMinSqlEvent updateMinSqlEvent;
    private UpdateMinEvent updateMinEvent;
    private UpdateSumEvent updateSumEvent;
    private UpdateSumSqlEvent updateSumSqlEvent;

    public MybatisAccessorConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.enhanceSqlInjector = new EnhanceSqlInjector();
        this.entityDtoServiceRelationMap = new EntityDtoServiceRelationMap(this.applicationContext);
        this.entityClassInfoHelper = new EntityClassInfoHelper();
        this.defaultConverter = new DefaultConverter();
        this.dtoClassInfoHelper = new DtoClassInfoHelper(this.applicationContext
                , this.entityDtoServiceRelationMap, this.entityClassInfoHelper);
        this.updateService = new UpdateService(this.dtoClassInfoHelper);
        this.dtoClassInfoHelper.setUpdateService(this.updateService);
        this.selectService = new SelectService(this.dtoClassInfoHelper);
        this.dtoClassInfoHelper.setSelectService(this.selectService);

        this.defaultDeleteProcessorProvider = new DefaultDeleteProcessorProvider(this.applicationContext);
        this.defaultDeleteByIdProcessorProvider = new DefaultDeleteByIdProcessorProvider(this.applicationContext);
        this.defaultInsertProcessorProvider = new DefaultInsertProcessorProvider(this.applicationContext);
        this.defaultUpdateProcessorProvider = new DefaultUpdateProcessorProvider(this.applicationContext, this.selectService);

        this.mybatisAccessorService = new MybatisAccessorService(this.entityDtoServiceRelationMap
                , this.selectService, this.updateService
                , this.defaultInsertProcessorProvider, this.defaultUpdateProcessorProvider, this.defaultDeleteByIdProcessorProvider);
        this.updateManager = new MybatisAccessorUpdateManager(this.mybatisAccessorService, this.dtoClassInfoHelper);
        this.scanRelationOnStartup = new ScanRelationOnStartup(this.applicationContext, this.entityDtoServiceRelationMap);

        TableInfoUtils.scanPackages.getAndSet(getMapperScanPackages(this.applicationContext));

        updateAssignEvent = new UpdateAssignEvent();
        updateAssignSqlEvent = new UpdateAssignSqlEvent();
        updateAvgAssignEvent = new UpdateAvgAssignEvent();
        updateAvgAssignSqlEvent = new UpdateAvgAssignSqlEvent();
        updateAvgEvent = new UpdateAssignEvent();
        updateAvgSqlEvent = new UpdateAssignSqlEvent();
        updateCountEvent = new UpdateCountEvent();
        updateCountSqlEvent = new UpdateCountSqlEvent();
        updateExecuteBeanMethodEvent = new UpdateExecuteBeanMethodEvent();
        updateExecuteSqlEvent = new UpdateExecuteSqlEvent();
        updateFillEvent = new UpdateFillEvent();
        updateMaxEvent = new UpdateMaxEvent();
        updateMaxSqlEvent = new UpdateMaxSqlEvent();
        updateMinSqlEvent = new UpdateMinSqlEvent();
        updateMinEvent = new UpdateMinEvent();
        updateSumEvent = new UpdateSumEvent();
        updateSumSqlEvent = new UpdateSumSqlEvent();
    }

    private List<String> getMapperScanPackages(ApplicationContext applicationContext) {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(MapperScan.class);
        return Arrays.stream(beanNames).map(x -> applicationContext.findAnnotationOnBean(x, MapperScan.class))
                .flatMap(x -> Stream.concat(Stream.of(x.value()), Stream.of(x.basePackages()))).collect(Collectors.toList());
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
    public IUpdateService getUpdateService() {
        return this.updateService;
    }

    @Bean
    public ISelectService getSelectService() {
        return this.selectService;
    }

    @Bean
    public DefaultDeleteByIdProcessorProvider getDefaultDeleteByIdProcessorProvider() {
        return this.defaultDeleteByIdProcessorProvider;
    }

    @Bean
    public DefaultDeleteProcessorProvider getDefaultDeleteProcessorProvider() {
        return this.defaultDeleteProcessorProvider;
    }


    @Bean
    public DefaultInsertProcessorProvider getDefaultInsertProcessorProvider() {
        return this.defaultInsertProcessorProvider;
    }

    @Bean
    public DefaultUpdateProcessorProvider getDefaultUpdateProcessorProvider() {
        return this.defaultUpdateProcessorProvider;
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
    public MybatisAccessorService getMybatisAccessorService() {
        return this.mybatisAccessorService;
    }

    @Bean
    public MybatisAccessorUpdateManager getMybatisAccessorUpdateManager() {
        return this.updateManager;
    }

    @Bean
    public DefaultConverter getDefaultConverter() {
        return this.defaultConverter;
    }
}
