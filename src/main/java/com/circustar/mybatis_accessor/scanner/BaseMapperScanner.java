package com.circustar.mybatis_accessor.scanner;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.circustar.common_utils.reflection.ClassUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import java.lang.reflect.Type;
import java.util.*;

public class BaseMapperScanner {
    private static Set<String> scannedPackages = new HashSet<>();
    private static HashMap<String, Set<Class<? extends BaseMapper>>> packageBaseMappers = new HashMap<>();
    private static Map<Class<?>, Set<Class<? extends BaseMapper>>> baseMapperMap = new HashMap<>();
    private static ClassPathScanningCandidateComponentProvider provider;
    static {
        provider = new InterfaceCandidateComponentProvider(false);
        provider.addIncludeFilter(new CustomInterfaceFilter(BaseMapper.class));
    }
    public static boolean packageScanned(String scanPackage) {
        return scannedPackages.contains(scanPackage);
    }
    public static synchronized void scan(String scanPackage) throws ClassNotFoundException {
        if(scannedPackages.contains(scanPackage)) {
            return;
        }
        Set<BeanDefinition> definitionSet = provider.findCandidateComponents(scanPackage);
        for(BeanDefinition bd: definitionSet) {
            Class<? extends BaseMapper> clazz = (Class<? extends BaseMapper>) Class.forName(bd.getBeanClassName());
            addBaseMapper(clazz);
            addBaseMapperToPackage(scanPackage, clazz);
        }
        scannedPackages.add(scanPackage);
    }

    private static void addBaseMapper(Class<? extends  BaseMapper> clazz) {
        Type actualTypeArgument = ClassUtils.getFirstTypeArgument(clazz);
        Set<Class<? extends BaseMapper>> mapperSet = null;
        if(baseMapperMap.containsKey(clazz)) {
            mapperSet = baseMapperMap.get((Class)actualTypeArgument);
        } else {
            mapperSet = new HashSet<>();
            baseMapperMap.put((Class)actualTypeArgument, mapperSet);
        }
        mapperSet.add(clazz);
    }

    public static Class<? extends BaseMapper> getBaseMapper(Class<?> clazz) {
        Set<Class<? extends BaseMapper>> mapperSet = baseMapperMap.get(clazz);
        if(mapperSet != null && !mapperSet.isEmpty()) {
            return mapperSet.stream().iterator().next();
        }
        return null;
    }

    private static void addBaseMapperToPackage(String pkg, Class<? extends  BaseMapper> clazz) {
        Set<Class<? extends BaseMapper>> baseMapperSet = null;
        if(packageBaseMappers.containsKey(pkg)) {
            baseMapperSet = packageBaseMappers.get(pkg);
        } else {
            baseMapperSet = new HashSet<>();
            packageBaseMappers.put(pkg, baseMapperSet);
        }
        baseMapperSet.add(clazz);
    }

    public static Set<Class<? extends BaseMapper>> getBaseMapperFromPackage(String pkg) {
        return packageBaseMappers.get(pkg);
    }
}
