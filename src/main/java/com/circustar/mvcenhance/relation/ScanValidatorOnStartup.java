package com.circustar.mvcenhance.relation;

import com.circustar.mvcenhance.config.EnableMvcEnhancement;
import com.circustar.mvcenhance.validator.DtoValidatorManager;
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

public class ScanValidatorOnStartup implements ApplicationRunner {
    public ScanValidatorOnStartup(DtoValidatorManager dtoValidatorManager) {
        this.dtoValidatorManager = dtoValidatorManager;
    }
    private DtoValidatorManager dtoValidatorManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        dtoValidatorManager.initValidatorMap();
    }
}
