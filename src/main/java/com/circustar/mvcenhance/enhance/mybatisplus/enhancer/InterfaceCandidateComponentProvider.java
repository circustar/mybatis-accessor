package com.circustar.mvcenhance.enhance.mybatisplus.enhancer;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;

public class InterfaceCandidateComponentProvider extends ClassPathScanningCandidateComponentProvider {
    public InterfaceCandidateComponentProvider(boolean useDefaultFilters) {
        super(useDefaultFilters);
    }
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isIndependent() && metadata.isInterface();
    }
}
