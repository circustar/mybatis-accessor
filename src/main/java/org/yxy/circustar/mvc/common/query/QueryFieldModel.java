package org.yxy.circustar.mvc.common.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;
import org.yxy.circustar.mvc.enhance.utils.SPELParser;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class QueryFieldModel {
    public static String ORDER_DESC = "desc";
    public static String ORDER_ASC = "asc";

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

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
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

    private String group;

    private String column;
    private String connector;
    private List<Object> values;
    private String sort_order;
    private Integer sort_index;

    public static <T> void setQueryWrapper(List<QueryFieldModel> conditions, QueryWrapper<T> queryWrapper) {
        conditions.forEach(x -> {
            Connector c = Connector.getConnectorByName(x.getConnector());
            if(c != null) {
                c.consume(x.column, queryWrapper, x.getValues());
            }
        });
        conditions.stream().filter(x -> x.sort_order!= null && !StringUtils.isEmpty(x.sort_order)).sorted((x, y) -> {
            return x.sort_index - y.sort_index;
        }).forEach(x -> {
            if(ORDER_DESC.equals(x.sort_order.toLowerCase())) {
                queryWrapper.orderByDesc(x.getColumn());
            } else {
                queryWrapper.orderByAsc(x.getColumn());
            }
        });
    }

//    public static List<QueryFieldModel> getUrlConditionList(List<QueryFieldModel> queryFiledModels) {
//        return queryFiledModels.stream().filter(x -> !StringUtils.isEmpty(x.getColumn()))
//                .map(x -> {
//                    QueryFieldModel queryFieldModel = new QueryFieldModel();
//                    queryFieldModel.setConnector(x.getConnector());
//                    queryFieldModel.setColumn(x.getColumn());
//                    queryFieldModel.setValues(x.getValues());
//                    return queryFieldModel;
////                    return QueryFieldModel.builder()
////                        .connector(x.getConnector())
////                        .column(x.getColumn())
////                        .values(x.getValues())
////                        .build();})
//                }).collect(Collectors.toList());
//    }

//    public static List<OrderItem> getOrderItemList(List<QueryFieldModel> queryFiledModels) {
//        return queryFiledModels.stream().filter(x -> !StringUtils.isEmpty(x.sort_order) && x.sort_index!= null)
//                .sorted((x, y) -> {
//                    return x.sort_index - y.sort_index;
//                })
//                .map(x -> new OrderItem(x.getColumn(), !ORDER_DESC.equals(x.sort_order)))
//                .collect(Collectors.toList());
//    }

    public static List<QueryFieldModel> getQueryFieldModeFromDto(Object dto) throws IllegalAccessException {
        return getQueryFieldModeFromDto(dto, "");
    }

    public static List<QueryFieldModel> getQueryFieldModeFromDto(Object dto, String groupName) throws IllegalAccessException {
        if(dto == null) {
            return null;
        }
        List<QueryFieldModel> result = new ArrayList<>();
        List<QueryFieldValue> QueryFieldValues = new ArrayList<>();
        StandardEvaluationContext context = new StandardEvaluationContext(dto);
//        ExpressionParser ep = new SpelExpressionParser();

        Field[] fields = dto.getClass().getDeclaredFields();
        for(Field f : fields) {
            f.setAccessible(true);
            Object fieldValue = f.get(dto);

            QueryField[] queryFields = f.getAnnotationsByType(QueryField.class);
            List<QueryFieldValue> fieldValues = Arrays.stream(queryFields)
                    .filter(x -> ((StringUtils.isEmpty(groupName) && x.group().length == 0)
                            || Arrays.stream(x.group()).anyMatch(y -> y.equals(groupName)))
                            && !StringUtils.isEmpty(x.connector())
                            &&(!x.ignoreEmpty() || !(fieldValue == null && "".equals(fieldValue))))
                    .map(x -> {
                        Object expressionValue = null;
                        if(x.expression() != null && !"".equals(x.expression()) ) {
//                            Expression expression = ep.parseExpression(x.expression());
//                            expressionValue = expression.getValue(context);
                            expressionValue = SPELParser.parseExpression(context, x.expression());
                        } else {
                            expressionValue = fieldValue;
                        }
                        if(expressionValue != null
                                && x.connector() == Connector.exists || x.connector() == Connector.notExists) {
                            expressionValue = expressionValue.toString().replace("'", "");
                        }
                        QueryFieldValue queryFieldValue = new QueryFieldValue();
                        queryFieldValue.setConnector(x.connector());
                        queryFieldValue.setColumn(StringUtils.isEmpty(x.column())?f.getName():x.column());
                        queryFieldValue.setGroup(groupName);
                        queryFieldValue.setSortIndex(x.sortIndex());
                        queryFieldValue.setSortOrder(x.sortOrder());
                        queryFieldValue.setValue(expressionValue);
                        return queryFieldValue;
//                        return QueryFieldValue.builder()
//                            .connector(x.connector())
//                            .column(StringUtils.isEmpty(x.column())?f.getName():x.column())
//                            .group(groupName)
//                            .sortIndex(x.sortIndex())
//                            .sortOrder(x.sortOrder())
//                            .value(expressionValue)
//                            .build();
                    }).collect(Collectors.toList());
            QueryFieldValues.addAll(fieldValues);
        }
        Map<String, List<QueryFieldValue>> mappedQueryFieldValues = QueryFieldValues.stream().collect(
                            Collectors.groupingBy(
                                    x -> x.column + ":" + x.connector
                            )
                    );
        for(String fieldName : mappedQueryFieldValues.keySet()) {
            List<QueryFieldValue> var0 = mappedQueryFieldValues.get(fieldName);
            QueryFieldValue var1 = var0.get(0);
            List<Object> values = var0.stream()
                    .map(x -> x.value)
                    .collect(Collectors.toList());
            QueryFieldModel queryFieldModel = new QueryFieldModel();
            queryFieldModel.setConnector(var1.getConnector().name());
            queryFieldModel.setColumn(var1.getColumn());
            queryFieldModel.setGroup(var1.getGroup());
            queryFieldModel.setSort_order(var1.getSortOrder());
            queryFieldModel.setSort_index(var1.getSortIndex());
            queryFieldModel.setValues(values);
            result.add(queryFieldModel);
//            result.add(QueryFieldModel.builder()
//                    .connector(var1.connector.name())
//                    .column(var1.column)
//                    .group(var1.group)
//                    .sort_order(var1.sortOrder)
//                    .sort_index(var1.sortIndex)
//                    .values(values)
//                    .build());
        }

        return result;

    }
}
