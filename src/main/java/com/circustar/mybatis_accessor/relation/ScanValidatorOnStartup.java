package com.circustar.mybatis_accessor.relation;

import com.circustar.mybatis_accessor.validator.DtoValidatorManager;
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
