package com.circustar.mybatis_accessor.annotation.after_update;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;

import java.util.List;

public interface IAfterUpdateExecutor {
    default ExecuteTiming getExecuteTiming() {return ExecuteTiming.AFTER_SUB_ENTITY_UPDATE;}
    void exec(DtoClassInfo dtoClassInfo, List<Object> dtoList, List<Object> entityList, String[] params);
}
