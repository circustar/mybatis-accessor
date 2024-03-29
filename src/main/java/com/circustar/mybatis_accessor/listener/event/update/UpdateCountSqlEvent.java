package com.circustar.mybatis_accessor.listener.event.update;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.common.MybatisAccessorException;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdateCountSqlEvent extends AbstractUpdateEvent<UpdateEventModel> implements IUpdateEvent<UpdateEventModel> {
    private static final String ORIGINAL_SQL = "select count(*) from %s t1 where t1.%s = %s.%s";

    @Override
    public ExecuteTiming getDefaultExecuteTiming() {
        return ExecuteTiming.AFTER_SUB_ENTITY_UPDATE;
    }

    @Override
    public IUpdateCommand.UpdateType[] getDefaultUpdateTypes() {
        return new IUpdateCommand.UpdateType[]{
                IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE
        };
    }

    @Override
    protected List<DtoField> parseDtoFieldList(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo) {
        String mFieldName = updateEventModel.getUpdateParams().get(0);
        String sFieldName = updateEventModel.getUpdateParams().get(1);
        DtoField mField = dtoClassInfo.getDtoField(mFieldName);
        DtoField sField = dtoClassInfo.getDtoField(sFieldName);

        ArrayList<DtoField> dtoFields = new ArrayList<>();
        dtoFields.add(mField);
        dtoFields.add(sField);
        return dtoFields;
    }

    @Override
    protected List<Object> parseParams(UpdateEventModel updateEventModel, List<DtoField> dtoFields, DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo) {
        TableInfo tableInfo = dtoClassInfo.getEntityClassInfo().getTableInfo();
        TableInfo subTableInfo = fieldDtoClassInfo.getEntityClassInfo().getTableInfo();
        String selectSql = createSqlPart(updateEventModel, dtoClassInfo, tableInfo, fieldDtoClassInfo, subTableInfo, dtoFields);
        return Collections.singletonList(selectSql);
    }

    protected String createSqlPart(UpdateEventModel updateEventModel, DtoClassInfo dtoClassInfo, TableInfo tableInfo
            , DtoClassInfo subDtoClassInfo, TableInfo subTableInfo, List<DtoField> dtoFields) {
        String upperKeyColumn = tableInfo.getKeyColumn();
        if(subTableInfo.equals(tableInfo)) {
            upperKeyColumn = dtoFields.get(0).getDtoClassInfo().getEntityClassInfo().getIdReferenceFieldInfo().getColumnName();
        }
        String selectSql = String.format(UpdateCountSqlEvent.ORIGINAL_SQL
                , subTableInfo.getTableName()
                , upperKeyColumn
                , tableInfo.getTableName()
                , tableInfo.getKeyColumn());

        if(subTableInfo.getLogicDeleteFieldInfo() != null) {
            selectSql = selectSql + " and t1." + subTableInfo.getLogicDeleteFieldInfo().getColumn() + " = " + subTableInfo.getLogicDeleteFieldInfo().getLogicNotDeleteValue();
        }
        return selectSql;
    }

    @Override
    protected void execUpdate(DtoClassInfo dtoClassInfo, DtoClassInfo fieldDtoClassInfo
            , List<Object> dtoList, List<DtoField> dtoFields, List<Object> parsedParams
            , String updateEventLogId) throws MybatisAccessorException {
        DtoField mField = dtoFields.get(0);
        IService serviceBean = dtoClassInfo.getServiceBean();
        String execSelectSql = parsedParams.get(0).toString();
        DtoField keyField = dtoClassInfo.getKeyField();
        UpdateWrapper updateWrapper = new UpdateWrapper();
        for(Object dto : dtoList) {
            Object keyValue = FieldUtils.getFieldValue(dto, keyField.getPropertyDescriptor().getReadMethod());

            updateWrapper.clear();
            updateWrapper.setSql(mField.getEntityFieldInfo().getColumnName() + " = (" + execSelectSql + ")");
            updateWrapper.eq(keyField.getEntityFieldInfo().getColumnName(), keyValue);
            serviceBean.update(updateWrapper);
        }
    }

    @Override
    protected DtoClassInfo getFieldDtoClassInfo(List<DtoField> dtoFields) {
        return dtoFields.get(1).getFieldDtoClassInfo();
    }
}
