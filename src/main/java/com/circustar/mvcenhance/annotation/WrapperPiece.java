package com.circustar.mvcenhance.annotation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mvcenhance.classInfo.DtoClassInfo;
import com.circustar.mvcenhance.classInfo.DtoField;
import com.circustar.mvcenhance.classInfo.FieldTypeInfo;
import com.circustar.mvcenhance.utils.FieldUtils;
import com.circustar.mvcenhance.utils.TableInfoUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WrapperPiece {
    private String tableName;
    private String columnName;
    private QueryFieldModel queryField;
    private OrderFieldModel orderField;
    private GroupFieldModel groupField;
    private Object value;

    public WrapperPiece(QueryField queryField, OrderField orderField
            , GroupField groupField, String tableName, String columnName, Object value) {
        this(queryField == null ? new QueryFieldModel(columnName) :new QueryFieldModel(queryField)
                , orderField == null ? null : new OrderFieldModel(orderField)
                , groupField == null ? null : new GroupFieldModel(groupField)
                , tableName, columnName, value);
    }

    public WrapperPiece(QueryFieldModel queryField, OrderFieldModel orderField
            , GroupFieldModel groupField, String tableName, String columnName, Object value) {
        this.queryField = queryField;
        this.orderField = orderField;
        this.groupField = groupField;
        this.columnName = columnName;
        this.tableName = tableName;
        this.value = value;

        if(StringUtils.isEmpty(this.queryField.queryExpression)) {
            this.queryField.setQueryExpression(this.tableName + "." + this.columnName);
        }
        if(this.orderField != null && StringUtils.isEmpty(this.orderField.getOrderExpression())) {
            this.orderField.setOrderExpression(this.tableName + "." + this.columnName);
        }
        if(this.groupField != null
                && StringUtils.isEmpty(this.groupField.getSelectExpression())
                && StringUtils.isEmpty(this.groupField.getGroupByExpression())) {
            this.groupField.setGroupByExpression(this.tableName + "." +this.columnName);
        }
    }

    public QueryFieldModel getQueryField() {
        return queryField;
    }

    public OrderFieldModel getOrderField() {
        return orderField;
    }

    public GroupFieldModel getGroupField() {
        return groupField;
    }

    public String getColumnName() {
        return columnName;
    }

    public Object getValue() {
        return value;
    }

    public void setQueryField(QueryFieldModel queryField) {
        this.queryField = queryField;
    }

    public void setOrderField(OrderFieldModel orderField) {
        this.orderField = orderField;
    }

    public void setGroupField(GroupFieldModel groupField) {
        this.groupField = groupField;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public static <T> QueryWrapper<T> createQueryWrapper(List<WrapperPiece> wrapperPieces) {
        QueryWrapper<T> qw = new QueryWrapper<>();
        for(WrapperPiece wrapperPiece : wrapperPieces) {
            if(wrapperPiece.queryField == null) {
                continue;
            }
            wrapperPiece.queryField.getConnector().consume(wrapperPiece.queryField.queryExpression, qw, wrapperPiece.value);
        }

        List<String> selectColumns = new ArrayList<>();
        for(WrapperPiece wrapperPiece : wrapperPieces) {
            if(wrapperPiece.groupField == null) {
                continue;
            }
            if(!StringUtils.isEmpty(wrapperPiece.groupField.getGroupByExpression())) {
                qw.groupBy(wrapperPiece.groupField.getGroupByExpression());
                if(StringUtils.isEmpty(wrapperPiece.groupField.getSelectExpression())) {
                    selectColumns.add(wrapperPiece.groupField.getGroupByExpression() + " as  " + wrapperPiece.getColumnName());
                }
            } else if(!StringUtils.isEmpty(wrapperPiece.groupField.getSelectExpression())) {
                selectColumns.add(wrapperPiece.groupField.getSelectExpression() + " as  " + wrapperPiece.getColumnName());
            } else {
                selectColumns.add(wrapperPiece.groupField.getGroupByExpression() + " as  " + wrapperPiece.getColumnName());
            }

            if(!StringUtils.isEmpty(wrapperPiece.groupField.getHavingExpression())) {
                qw.having(wrapperPiece.groupField.getHavingExpression());
            }
        }
        qw.select(selectColumns.toArray(new String[selectColumns.size()]));

        wrapperPieces.stream().filter(x -> x.getOrderField() != null)
                .map(x -> x.getOrderField())
                .sorted(Comparator.comparingInt(OrderFieldModel::getSortIndex)).forEach(x -> {
            if(OrderField.ORDER_DESC.equals(x.getSortOrder())) {
                qw.orderByDesc(x.getOrderExpression());
            } else {
                qw.orderByAsc(x.getOrderExpression());
            }
        });

        return qw;
    }

    public static List<WrapperPiece> getQueryWrapperFromDto(DtoClassInfo dtoClassInfo, Object dto) throws IllegalAccessException {
        List<WrapperPiece> wrapperPieces = new ArrayList<>();
        for(DtoField dtoField : dtoClassInfo.getNormalFieldList()) {
            Object fieldValue = FieldUtils.getValue(dto, dtoField.getFieldTypeInfo().getField());
            FieldTypeInfo entityField = dtoClassInfo.getEntityClassInfo().getFieldByName(dtoField.getFieldName());
            String tableName = dtoClassInfo.getEntityClassInfo().getTableInfo().getTableName();
            String defaultColumnName = null;
            if(entityField != null) {
                defaultColumnName = TableInfoUtils.getDBObjectName(entityField.getField().getName());
            }
            WrapperPiece wp = new WrapperPiece(dtoField.getQueryField(), dtoField.getOrderField()
                    , dtoField.getGroupField(), tableName, defaultColumnName, fieldValue);
            wrapperPieces.add(wp);
        }
        return wrapperPieces;
    }
}
