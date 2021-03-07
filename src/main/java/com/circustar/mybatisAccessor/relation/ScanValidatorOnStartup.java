package com.circustar.mybatisAccessor.relation;

import com.circustar.mybatisAccessor.validator.DtoValidatorManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

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
