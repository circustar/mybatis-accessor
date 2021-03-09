package com.circustar.mybatis_accessor.relation;

import com.circustar.mybatis_accessor.annotation.DtoEntityRelation;
import com.circustar.mybatis_accessor.annotation.DtoEntityRelations;
import com.circustar.mybatis_accessor.annotation.RelationScanPackages;
import com.circustar.mybatis_accessor.annotation.EnableMybatisAccessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ScanRelationOnStartup implements ApplicationRunner {
    public static boolean enableSpringValidation = false;
    public ScanRelationOnStartup(ApplicationContext context, IEntityDtoServiceRelationMap relationMap) {
        this.context = context;
        this.relationMap = relationMap;
    }
    private IEntityDtoServiceRelationMap relationMap;

    private ApplicationContext context;

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
        for(String className : beans.keySet()) {
            //Class<?> targetClass = AopUtils.getTargetClass(beans.get(className));
            Class<?> targetClass = getTargetClass(beans.get(className).getClass());
            RelationScanPackages[] annotations = targetClass.getAnnotationsByType(RelationScanPackages.class);
            scanForRelationMap(Arrays.stream(annotations).map(x -> x.value())
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList()));
        }
        Map<String, Object> enableAutoController = context.getBeansWithAnnotation(EnableMybatisAccessor.class);
        for(String className : enableAutoController.keySet()) {
            Class<?> targetClass = getTargetClass(enableAutoController.get(className).getClass());
            EnableMybatisAccessor[] annotations = targetClass.getAnnotationsByType(EnableMybatisAccessor.class);
            List<String> scannList = Arrays.stream(annotations).map(x -> x.relationScan().value())
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());
            scanForRelationMap(scannList);
            if(annotations.length > 0) {
                ScanRelationOnStartup.enableSpringValidation = annotations[0].enableSpringValidation();
            }
        }
    }

    protected void scanForRelationMap(List<String> packageNameList) throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider p = new ClassPathScanningCandidateComponentProvider(false);
        p.addIncludeFilter(new AnnotationTypeFilter(DtoEntityRelation.class));
        p.addIncludeFilter(new AnnotationTypeFilter(DtoEntityRelations.class));
        for(String packageName : packageNameList) {
            Set<BeanDefinition> definitionSet = p.findCandidateComponents(packageName);
            for(BeanDefinition bd: definitionSet) {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                DtoEntityRelation[] dtoEntityRelations = clazz.getAnnotationsByType(DtoEntityRelation.class);
                Arrays.stream(dtoEntityRelations)
                        .forEach(x -> relationMap.addRelation(new EntityDtoServiceRelation(
                                x.dtoClass() == Void.class?clazz : x.dtoClass()
                                , x.entityClass() == Void.class?clazz : x.entityClass()
                                , x.service())));

            }
        }
    }
}