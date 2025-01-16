package com.circustar.mybatis_accessor.relation;

import com.circustar.mybatis_accessor.annotation.scan.DtoEntityRelation;
import com.circustar.mybatis_accessor.annotation.scan.DtoEntityRelations;
import com.circustar.mybatis_accessor.annotation.scan.RelationScanPackages;
import com.circustar.mybatis_accessor.annotation.scan.EnableMybatisAccessor;
import com.circustar.mybatis_accessor.class_info.DtoClassInfoHelper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ScanRelationOnStartup implements ApplicationRunner {
    private final IEntityDtoServiceRelationMap relationMap;
    private final ApplicationContext context;

    public ScanRelationOnStartup(ApplicationContext context, IEntityDtoServiceRelationMap relationMap) {
        this.context = context;
        this.relationMap = relationMap;
    }

    private Class getTargetClass(Class clazz) {
        if(clazz == null) {
            return null;
        } else if(clazz.getName().contains("$$")) {
            return getTargetClass(clazz.getSuperclass());
        }
        return clazz;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, Object> beans = context.getBeansWithAnnotation(RelationScanPackages.class);
        for(Map.Entry<String, Object> target : beans.entrySet()) {
            Class<?> targetClass = getTargetClass(target.getValue().getClass());
            RelationScanPackages[] annotations = targetClass.getAnnotationsByType(RelationScanPackages.class);
            scanForRelationMap(Arrays.stream(annotations).map(x -> x.value())
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList()));
        }
        List<String> allWatchList = new ArrayList<>();
        Map<String, Object> enableAutoController = context.getBeansWithAnnotation(EnableMybatisAccessor.class);
        Class<?> targetClass = null;
        for(Map.Entry<String, Object> classEntry : enableAutoController.entrySet()) {
            targetClass = getTargetClass(classEntry.getValue().getClass());
            EnableMybatisAccessor[] annotations = targetClass.getAnnotationsByType(EnableMybatisAccessor.class);
            List<String> scannList = Arrays.stream(annotations).map(x -> x.relationScan().value())
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());
            scanForRelationMap(scannList);

            List<String> watchList = Arrays.stream(annotations).filter(x -> x.detectDtoChanges())
                    .map(x -> x.relationScan().value())
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());
            allWatchList.addAll(watchList);
        }
        if(targetClass != null && !CollectionUtils.isEmpty(allWatchList)) {
            final String codePath = targetClass.getProtectionDomain().getCodeSource().getLocation().getPath();
            registerWatchService(codePath.contains(":")?codePath.substring(1) : codePath, allWatchList);
        }
    }

    protected void scanForRelationMap(List<String> packageNameList) throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(DtoEntityRelation.class));
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(DtoEntityRelations.class));
        for(String packageName : packageNameList) {
            Set<BeanDefinition> definitionSet = componentProvider.findCandidateComponents(packageName);
            for(BeanDefinition bd: definitionSet) {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                DtoEntityRelation[] dtoEntityRelations = clazz.getAnnotationsByType(DtoEntityRelation.class);
                Arrays.stream(dtoEntityRelations)
                        .forEach(x -> relationMap.addRelation(new EntityDtoServiceRelation(
                                x.dtoClass() == Void.class?clazz : x.dtoClass()
                                , x.entityClass() == Void.class?clazz : x.entityClass()
                                , x.name()
                                , x.service(), x.convertDtoToEntityClazz(), x.convertEntityToDtoClazz())));

            }
        }
    }

    protected void registerWatchService(String codePath, List<String> packageNameList) {
        new Thread(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                for (String packageName : packageNameList) {
                    Path path = Paths.get(codePath + packageName.replace('.', '/') + "/");
                    path.register(watchService, StandardWatchEventKinds.ENTRY_DELETE);
                }
                while (true) {
                    final WatchKey key = watchService.take();
                    final List<WatchEvent<?>> watchEvents = key.pollEvents();
                    if (!CollectionUtils.isEmpty(watchEvents)) {
                        System.out.println("file system changed");
                    }
                    key.reset();
                    DtoClassInfoHelper.resetMap();
                    TimeUnit.SECONDS.sleep(10);
                }
            } catch (NoSuchFileException noSuchFileException) {
                System.out.println("FILE NOT EXIST：" + noSuchFileException.getFile());
            } catch (InterruptedException|IOException e) {
                e.printStackTrace();
            }
        }).start();

    }
}
