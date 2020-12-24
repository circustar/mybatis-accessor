package com.circustar.mvcenhance.enhance.mybatisplus.enhancer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class BaseMapperScanner {
    private static Set<Package> scannedPackages = new HashSet<>();
    private static Map<Class<?>, Set<Class<? extends BaseMapper>>> baseMapperMap = new HashMap<>();
    private static ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
    static {
        provider = new ClassPathScanningCandidateComponentProvider(false);
    }
    public static synchronized void scan(Package scanPackage) throws ClassNotFoundException {
        if(scannedPackages.contains(scanPackage)) {
            return;
        }
        Set<BeanDefinition> definitionSet = provider.findCandidateComponents(scanPackage.getName());
        for(BeanDefinition bd: definitionSet) {
            Class<? extends BaseMapper> clazz = (Class<? extends BaseMapper>) Class.forName(bd.getBeanClassName());
            putBaseMapper(clazz);
        }
        scannedPackages.add(scanPackage);
    }

    private static void putBaseMapper(Class<? extends  BaseMapper> clazz) {
        ParameterizedType parametclass = (ParameterizedType) clazz.getGenericSuperclass();
        Type actualTypeArgument = parametclass.getActualTypeArguments()[0];
        Set<Class<? extends BaseMapper>> mapperSet = baseMapperMap.get((Class)actualTypeArgument);
        if(mapperSet == null) {
            mapperSet = new HashSet<>();
            baseMapperMap.put((Class)actualTypeArgument, mapperSet);
        }
        if(!mapperSet.contains(clazz)) {
            mapperSet.add(clazz);
        }
    }

    public static Class<? extends BaseMapper> getBaseMapper(Class<?> clazz) {
        Set<Class<? extends BaseMapper>> mapperSet = baseMapperMap.get(clazz);
        if(mapperSet != null && mapperSet.size() > 0) {
            return mapperSet.stream().iterator().next();
        }
        return null;
    }

}
