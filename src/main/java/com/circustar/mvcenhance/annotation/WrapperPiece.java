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
    private String columnName;
    private QueryFieldModel queryField;
    private OrderFieldModel orderField;
    private GroupFieldModel groupField;
    private Object value;

    public WrapperPiece(QueryField queryField, OrderField orderField
            , GroupField groupField, String columnName, Object value) {
        this(new QueryFieldModel(queryField), new OrderFieldModel(orderField),
                new GroupFieldModel(groupField), columnName, value);
    }

    public WrapperPiece(QueryFieldModel queryField, OrderFieldModel orderField
            , GroupFieldModel groupField, String columnName, Object value) {
        this.queryField = queryField;
        this.orderField = orderField;
        this.groupField = groupField;
        this.columnName = columnName;
        if(StringUtils.isEmpty(this.queryField.queryExpression)) {
            this.queryField.setQueryExpression(this.columnName);
        }
        if(StringUtils.isEmpty(this.orderField.getOrderExpression())) {
            this.orderField.setOrderExpression(this.columnName);
        }
        this.value = value;
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

        for(WrapperPiece wrapperPiece : wrapperPieces) {
            if(wrapperPiece.groupField == null) {
                continue;
            }
            if(!StringUtils.isEmpty(wrapperPiece.groupField.getGroupByExpression())) {
                qw.groupBy(wrapperPiece.groupField.getGroupByExpression());
                if(StringUtils.isEmpty(wrapperPiece.groupField.getSelectExpression())) {
                    qw.select(wrapperPiece.groupField.getGroupByExpression() + " as  " + wrapperPiece.getColumnName());
                }
            } else if(!StringUtils.isEmpty(wrapperPiece.groupField.getSelectExpression())) {
                qw.select(wrapperPiece.groupField.getSelectExpression() + " as  " + wrapperPiece.getColumnName());
            }

            if(!StringUtils.isEmpty(wrapperPiece.groupField.getHavingExpression())) {
                qw.having(wrapperPiece.groupField.getHavingExpression());
            }
        }

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
            String defaultColumnName = null;
            if(entityField != null) {
                defaultColumnName = TableInfoUtils.getDBObjectName(entityField.getField().getName());
            }
            WrapperPiece wp = new WrapperPiece(dtoField.getQueryField(), dtoField.getOrderField()
                    , dtoField.getGroupField(), defaultColumnName, fieldValue);
            wrapperPieces.add(wp);
        }
        return wrapperPieces;
    }
}
