package com.circustar.mybatis_accessor.annotation.after_update_executor;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfoHelper;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.relation.IEntityDtoServiceRelationMap;
import org.springframework.context.ApplicationContext;

import java.util.List;

public interface IAfterUpdateExecutor {
    ExecuteTiming getExecuteTiming();
    void exec(DtoClassInfo dtoClassInfo, List<Object> dtoList, List<Object> entityList, String[] params);
}
