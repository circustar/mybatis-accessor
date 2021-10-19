package com.circustar.mybatis_accessor.annotation.listener;

import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.List;

public abstract class AbstractUpdateEvent implements IUpdateEvent {
    protected abstract List<DtoField> parseDtoFieldList(DtoClassInfo dtoClassInfo, String[] params);

    protected abstract List<Object> parseParams(List<DtoField> dtoFields, DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo, String[] originParams);

    protected abstract void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo, List<Object> entityList, List<DtoField> dtoFields, List<Object> parsedParams);

    protected abstract DtoClassInfo getFieldDtoClassInfo(List<DtoField> dtoFields);

    @Override
    public void exec(IUpdateCommand.UpdateType updateType
            , DtoClassInfo dtoClassInfo, List<Object> dtoList, List<Object> entityList, String[] params) {
        List<DtoField> dtoFields = parseDtoFieldList(dtoClassInfo, params);
        DtoClassInfo fieldDtoClassInfo = getFieldDtoClassInfo(dtoFields);
        List<Object> parsedParams = parseParams(dtoFields, dtoClassInfo, fieldDtoClassInfo, params);
        execUpdate(dtoClassInfo, fieldDtoClassInfo, entityList, dtoFields, parsedParams);
    }
}
