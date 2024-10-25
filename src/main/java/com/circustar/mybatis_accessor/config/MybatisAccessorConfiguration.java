package com.circustar.mybatis_accessor.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.circustar.mybatis_accessor.class_info.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.class_info.EntityClassInfoHelper;
import com.circustar.mybatis_accessor.converter.DefaultConverter;
import com.circustar.mybatis_accessor.listener.event.update.*;
import com.circustar.mybatis_accessor.provider.*;
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
    private final IEntityDtoServiceRelationMap entityDtoServiceRelationMap;
    private final EnhanceSqlInjector enhanceSqlInjector;
    private final ScanRelationOnStartup scanRelationOnStartup;
    private final IUpdateService updateService;
    private final ISelectService selectService;
    private final EntityClassInfoHelper entityClassInfoHelper;
    private final DtoClassInfoHelper dtoClassInfoHelper;
    private final MybatisAccessorService mybatisAccessorService;
    private final MybatisAccessorUpdateManager updateManager;
    private final DefaultDeleteByIdProcessorProvider defaultDeleteByIdProcessorProvider;
    private final DefaultDeleteProcessorProvider defaultDeleteProcessorProvider;
    private final DefaultInsertProcessorProvider defaultInsertProcessorProvider;
    private final DefaultUpdateProcessorProvider defaultUpdateProcessorProvider;
    private final DefaultSaveOrUpdateProcessorProvider defaultSaveOrUpdateProcessorProvider;
    private final DefaultConverter defaultConverter;

    private final UpdateAssignEvent updateAssignEvent;
    private final UpdateAssignSqlEvent updateAssignSqlEvent;
    private final UpdateAvgAssignEvent updateAvgAssignEvent;
    private final UpdateAvgAssignSqlEvent updateAvgAssignSqlEvent;
    private final UpdateAvgEvent updateAvgEvent;
    private final UpdateAvgSqlEvent updateAvgSqlEvent;
    private final UpdateCountEvent updateCountEvent;
    private final UpdateCountSqlEvent updateCountSqlEvent;
    private final UpdateExecuteBeanMethodEvent updateExecuteBeanMethodEvent;
    private final UpdateExecuteSqlEvent updateExecuteSqlEvent;
    private final UpdateFillEvent updateFillEvent;
    private final UpdateMaxEvent updateMaxEvent;
    private final UpdateMaxSqlEvent updateMaxSqlEvent;
    private final UpdateMinSqlEvent updateMinSqlEvent;
    private final UpdateMinEvent updateMinEvent;
    private final UpdateSumEvent updateSumEvent;
    private final UpdateSumSqlEvent updateSumSqlEvent;

    public MybatisAccessorConfiguration(ApplicationContext applicationContext) {
        this.enhanceSqlInjector = new EnhanceSqlInjector();
        this.entityDtoServiceRelationMap = new EntityDtoServiceRelationMap(applicationContext);
        this.entityClassInfoHelper = new EntityClassInfoHelper();
        this.defaultConverter = new DefaultConverter();
        this.dtoClassInfoHelper = new DtoClassInfoHelper(applicationContext
                , this.entityDtoServiceRelationMap, this.entityClassInfoHelper);
        this.updateService = new UpdateService(this.dtoClassInfoHelper);
        this.dtoClassInfoHelper.setUpdateService(this.updateService);
        this.selectService = new SelectService(this.dtoClassInfoHelper);
        this.dtoClassInfoHelper.setSelectService(this.selectService);

        this.defaultDeleteProcessorProvider = new DefaultDeleteProcessorProvider(applicationContext);
        this.defaultDeleteByIdProcessorProvider = new DefaultDeleteByIdProcessorProvider(applicationContext);
        this.defaultInsertProcessorProvider = new DefaultInsertProcessorProvider(applicationContext);
        this.defaultUpdateProcessorProvider = new DefaultUpdateProcessorProvider(applicationContext, this.selectService);
        this.defaultSaveOrUpdateProcessorProvider = new DefaultSaveOrUpdateProcessorProvider(applicationContext, this.selectService);

        this.mybatisAccessorService = new MybatisAccessorService(this.entityDtoServiceRelationMap
                , this.selectService, this.updateService
                , this.defaultInsertProcessorProvider, this.defaultUpdateProcessorProvider, this.defaultDeleteByIdProcessorProvider
                , this.defaultSaveOrUpdateProcessorProvider);
        this.updateManager = new MybatisAccessorUpdateManager(this.mybatisAccessorService, this.dtoClassInfoHelper);
        this.scanRelationOnStartup = new ScanRelationOnStartup(applicationContext, this.entityDtoServiceRelationMap);

        TableInfoUtils.SCAN_PACKAGES.getAndSet(getMapperScanPackages(applicationContext));

        updateAssignEvent = new UpdateAssignEvent(this.mybatisAccessorService);
        updateAssignSqlEvent = new UpdateAssignSqlEvent();
        updateAvgAssignEvent = new UpdateAvgAssignEvent(this.mybatisAccessorService);
        updateAvgAssignSqlEvent = new UpdateAvgAssignSqlEvent();
        updateAvgEvent = new UpdateAvgEvent(this.mybatisAccessorService);
        updateAvgSqlEvent = new UpdateAvgSqlEvent();
        updateCountEvent = new UpdateCountEvent(this.mybatisAccessorService);
        updateCountSqlEvent = new UpdateCountSqlEvent();
        updateExecuteBeanMethodEvent = new UpdateExecuteBeanMethodEvent();
        updateExecuteSqlEvent = new UpdateExecuteSqlEvent();
        updateFillEvent = new UpdateFillEvent(this.mybatisAccessorService);
        updateMaxEvent = new UpdateMaxEvent(this.mybatisAccessorService);
        updateMaxSqlEvent = new UpdateMaxSqlEvent();
        updateMinEvent = new UpdateMinEvent(this.mybatisAccessorService);
        updateMinSqlEvent = new UpdateMinSqlEvent();
        updateSumEvent = new UpdateSumEvent(this.mybatisAccessorService);
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
    public DefaultSaveOrUpdateProcessorProvider getDefaultSaveOrUpdateProcessorProvider() {
        return this.defaultSaveOrUpdateProcessorProvider;
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

    @Bean
    public UpdateAssignEvent getUpdateAssignEvent() {
        return this.updateAssignEvent;
    }

    @Bean
    public UpdateAssignSqlEvent getUpdateAssignSqlEvent() {
        return updateAssignSqlEvent;
    }

    @Bean
    public UpdateAvgAssignEvent getUpdateAvgAssignEvent() {
        return updateAvgAssignEvent;
    }

    @Bean
    public UpdateAvgAssignSqlEvent getUpdateAvgAssignSqlEvent() {
        return updateAvgAssignSqlEvent;
    }

    @Bean
    public UpdateAvgEvent getUpdateAvgEvent() {
        return updateAvgEvent;
    }

    @Bean
    public UpdateAvgSqlEvent getUpdateAvgSqlEvent() {
        return updateAvgSqlEvent;
    }

    @Bean
    public UpdateCountEvent getUpdateCountEvent() {
        return updateCountEvent;
    }

    @Bean
    public UpdateCountSqlEvent getUpdateCountSqlEvent() {
        return updateCountSqlEvent;
    }

    @Bean
    public UpdateExecuteBeanMethodEvent getUpdateExecuteBeanMethodEvent() {
        return updateExecuteBeanMethodEvent;
    }

    @Bean
    public UpdateExecuteSqlEvent getUpdateExecuteSqlEvent() {
        return updateExecuteSqlEvent;
    }

    @Bean
    public UpdateFillEvent getUpdateFillEvent() {
        return updateFillEvent;
    }

    @Bean
    public UpdateMaxEvent getUpdateMaxEvent() {
        return updateMaxEvent;
    }

    @Bean
    public UpdateMaxSqlEvent getUpdateMaxSqlEvent() {
        return updateMaxSqlEvent;
    }

    @Bean
    public UpdateMinSqlEvent getUpdateMinSqlEvent() {
        return updateMinSqlEvent;
    }

    @Bean
    public UpdateMinEvent getUpdateMinEvent() {
        return updateMinEvent;
    }

    @Bean
    public UpdateSumEvent getUpdateSumEvent() {
        return updateSumEvent;
    }

    @Bean
    public UpdateSumSqlEvent getUpdateSumSqlEvent() {
        return updateSumSqlEvent;
    }
}
