package com.circustar.mvcenhance.common.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.mvcenhance.enhance.field.DtoClassInfo;
import com.circustar.mvcenhance.enhance.field.DtoField;
import com.circustar.mvcenhance.enhance.field.FieldTypeInfo;
import com.circustar.mvcenhance.enhance.mybatisplus.enhancer.TableInfoUtils;
import com.circustar.mvcenhance.enhance.utils.FieldUtils;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;
import com.circustar.mvcenhance.enhance.utils.SPELParser;

import java.util.*;
import java.util.stream.Collectors;

public class QueryFieldModel {
    public static String ORDER_DESC = "desc";
    public static String ORDER_ASC = "asc";

    private String group;
    private String column;
    private String connector;
    private Object[] values;
    private String sort_order;
    private Integer sort_index;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getConnector() {
        return connector;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    public String getSort_order() {
        return sort_order;
    }

    public void setSort_order(String sort_order) {
        this.sort_order = sort_order;
    }

    public Integer getSort_index() {
        return sort_index;
    }

    public void setSort_index(Integer sort_index) {
        this.sort_index = sort_index;
    }

    public static <T> void setQueryWrapper(String tableName, List<QueryFieldModel> conditions, QueryWrapper<T> queryWrapper) {
        conditions.forEach(x -> {
            Connector c = Connector.getConnectorByName(x.getConnector());
            if(c != null) {
                c.consume(tableName + "." + x.column, queryWrapper, x.getValues());
            }
        });
        conditions.stream().filter(x -> x.sort_index!= null && x.sort_index != Integer.MAX_VALUE)
                .sorted((x, y) -> {
            return x.sort_index - y.sort_index;
        }).forEach(x -> {
            if(ORDER_DESC.equals(x.sort_order.toLowerCase())) {
                queryWrapper.orderByDesc(x.getColumn());
            } else {
                queryWrapper.orderByAsc(x.getColumn());
            }
        });
    }

    public static List<QueryFieldModel> getQueryFieldModeFromDto(DtoClassInfo dtoClassInfo, Object dto) throws NoSuchFieldException, IllegalAccessException {
        return getQueryFieldModeFromDto(dtoClassInfo, dto, "");
    }

    public static List<QueryFieldModel> getQueryFieldModeFromDto(DtoClassInfo dtoClassInfo, Object dto, String groupName) throws NoSuchFieldException, IllegalAccessException {
        List<QueryFieldModel> result = new ArrayList<>();
        List<QueryFieldImpl> allQueryFields = new ArrayList<>();
        StandardEvaluationContext context = new StandardEvaluationContext(dto);
        for(DtoField dtoField : dtoClassInfo.getNormalFieldList()) {
            Object fieldValue = FieldUtils.getValue(dto, dtoField.getFieldTypeInfo().getField());
            Set<QueryField> queryFieldSet = dtoField.getQueryField(groupName);
            FieldTypeInfo entityField = dtoClassInfo.getEntityClassInfo().getFieldByName(dtoField.getFieldName());
            String defaultColumnName = null;
            if(entityField != null) {
                defaultColumnName = TableInfoUtils.getDBObjectName(entityField.getField().getName());
            }
            List<QueryFieldImpl> queryFieldImpls = null;
            if(queryFieldSet == null  || queryFieldSet.size() == 0) {
                if(entityField == null) {
                    continue;
                }
                QueryFieldImpl queryFieldImpl = new QueryFieldImpl(defaultColumnName, fieldValue);
                queryFieldImpls = new ArrayList<>();
                queryFieldImpls.add(queryFieldImpl);
            } else {
                String finalDefaultColumnName = defaultColumnName;
                queryFieldImpls = dtoField.getQueryField(groupName).stream().map(x -> {
                    Object expressionValue = null;
                    if(!StringUtils.isEmpty(x.expression())) {
                        expressionValue = SPELParser.parseExpression(context, x.expression());
                    } else {
                        expressionValue = fieldValue;
                    }
                    if(expressionValue != null
                            && x.connector() == Connector.exists || x.connector() == Connector.notExists) {
                        expressionValue = expressionValue.toString().replace("'", "");
                    }
                    QueryFieldImpl queryFieldImpl = new QueryFieldImpl();
                    queryFieldImpl.setConnector(x.connector());
                    queryFieldImpl.setColumn(StringUtils.isEmpty(x.column())? finalDefaultColumnName :x.column());
                    queryFieldImpl.setGroup(groupName);
                    queryFieldImpl.setSortIndex(x.sortIndex());
                    queryFieldImpl.setSortOrder(x.sortOrder());
                    queryFieldImpl.setValue(expressionValue);
                    queryFieldImpl.setExpression(x.expression());
                    return queryFieldImpl;
                }).collect(Collectors.toList());
            }
            allQueryFields.addAll(queryFieldImpls);
        }
        Map<String, List<QueryFieldImpl>> queryFieldByColumn = allQueryFields.stream().collect(
                            Collectors.groupingBy(
                                    x -> x.column
                            )
                    );
        for(String columnName : queryFieldByColumn.keySet()) {
            List<QueryFieldImpl> queryFieldList = queryFieldByColumn.get(columnName);
            Map<Connector, List<QueryFieldImpl>> queryFieldByConnector = queryFieldList.stream().collect(Collectors.groupingBy(x -> x.connector));

            QueryFieldImpl minSortIndexQueryField = queryFieldList.stream().filter(x -> x.sortIndex != Integer.MAX_VALUE)
                    .min((x, y) -> x.sortIndex - y.sortIndex).orElse(null);
            QueryFieldImpl minSortOrderQueryField = queryFieldList.stream().filter(x -> !StringUtils.isEmpty(x.column)).findFirst().orElse(null);
            Integer sortIndex = minSortIndexQueryField == null ? null : minSortIndexQueryField.sortIndex;
            String sortOrder = minSortOrderQueryField == null ? QueryFieldModel.ORDER_ASC : minSortOrderQueryField.sortOrder;

            int i = 0;
            for(Connector connector : queryFieldByConnector.keySet()) {
                List<QueryFieldImpl> queryFieldConnectorList = queryFieldByConnector.get(connector);
                Object[] values = queryFieldConnectorList.stream()
                        .map(x -> x.value).toArray();
                QueryFieldModel queryFieldModel = new QueryFieldModel();
                queryFieldModel.setConnector(connector.name());
                queryFieldModel.setColumn(columnName);
                queryFieldModel.setGroup(groupName);
                queryFieldModel.setValues(values);
                if(i == 0 && sortIndex != null) {
                    queryFieldModel.setSort_index(sortIndex);
                    queryFieldModel.setSort_order(sortOrder);
                }
                result.add(queryFieldModel);
                i ++;
            }
        }
        return result;
    }
}
