package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.List;

public abstract class AbstractUpdateEvent<T> implements IUpdateEvent<T> {
    protected abstract List<DtoField> parseDtoFieldList(T updateEvent, DtoClassInfo dtoClassInfo);

    protected abstract List<Object> parseParams(T updateEvent, List<DtoField> dtoFields, DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo);

    protected abstract void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo
            , List<Object> dtoList, List<DtoField> dtoFields, List<Object> parsedParams);

    protected abstract DtoClassInfo getFieldDtoClassInfo(List<DtoField> dtoFields);

    @Override
    public IUpdateCommand.UpdateType[] getDefaultUpdateTypes() {
        return new IUpdateCommand.UpdateType[] {IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE};
    }

    @Override
    public void exec(T updateEvent, IUpdateCommand.UpdateType updateType
            , DtoClassInfo dtoClassInfo, List<Object> dtoList, String updateId, int level) {
        List<DtoField> dtoFields = parseDtoFieldList(updateEvent, dtoClassInfo);
        DtoClassInfo fieldDtoClassInfo = getFieldDtoClassInfo(dtoFields);
        List<Object> parsedParams = parseParams(updateEvent, dtoFields, dtoClassInfo, fieldDtoClassInfo);
        execUpdate(dtoClassInfo, fieldDtoClassInfo, dtoList, dtoFields, parsedParams);
    }
}
