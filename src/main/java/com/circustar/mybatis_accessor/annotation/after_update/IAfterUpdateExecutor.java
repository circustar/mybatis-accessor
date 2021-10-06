package com.circustar.mybatis_accessor.annotation.after_update;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IAfterUpdateExecutor {
    default ExecuteTiming getExecuteTiming() {return ExecuteTiming.AFTER_UPDATE;}
    void exec(DtoClassInfo dtoClassInfo, List<Object> dtoList, List<Object> entityList, String[] params);
}
