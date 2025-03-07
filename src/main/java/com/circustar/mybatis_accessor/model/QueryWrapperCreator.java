package com.circustar.mybatis_accessor.model;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.circustar.common_utils.parser.SPELParser;
import com.circustar.mybatis_accessor.annotation.Connector;
import com.circustar.mybatis_accessor.annotation.dto.QueryOrder;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.class_info.EntityClassInfo;
import com.circustar.common_utils.reflection.FieldUtils;
import com.circustar.mybatis_accessor.utils.TableJoinColumnPrefixManager;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class QueryWrapperCreator {
    private final String tableName;
    private final List<QuerySelectModel> querySelectModels;
    private final List<QueryWhereModel> queryWhereModels;
    private final List<QueryGroupByModel> queryGroupByModels;
    private final List<QueryHavingModel> queryHavingModels;
    private final List<QueryOrderModel> queryOrders;
    private final DtoClassInfo dtoClassInfo;
    private final EntityClassInfo entityClassInfo;
    private List<DtoField> joinTableDtoFields;
    private QueryWrapperBuilder baseWrapperBuilder;

    public QueryWrapperCreator(DtoClassInfo dtoClassInfo) {
        this.dtoClassInfo = dtoClassInfo;
        this.entityClassInfo = dtoClassInfo.getEntityClassInfo();
        this.tableName = this.entityClassInfo.getTableInfo().getTableName();
        this.queryGroupByModels = dtoClassInfo.getAllFieldList()
                .stream().filter(x -> x.getQueryGroupBy() != null)
                .map(x -> new QueryGroupByModel(x.getQueryGroupBy()
                        , this.entityClassInfo.getTableInfo().getTableName()
                        , x.getEntityFieldInfo().getColumnName()))
                .collect(Collectors.toList());

        this.queryWhereModels = dtoClassInfo.getAllFieldList()
                .stream().filter(x -> x.getQueryWhere() != null || x.getEntityFieldInfo() != null)
                .map(x -> new QueryWhereModel(x.getQueryWhere()
                        , this.tableName, x))
                .collect(Collectors.toList());
        this.queryOrders = dtoClassInfo.getAllFieldList()
                .stream().filter(x -> x.getQueryOrder() != null)
                .map(x -> new QueryOrderModel(x))
                .sorted(Comparator.comparingInt(QueryOrderModel::getSortIndex))
                .collect(Collectors.toList());

        if(!this.queryGroupByModels.isEmpty()) {
            this.queryHavingModels = dtoClassInfo.getAllFieldList()
                    .stream().filter(x -> x.getQueryHaving() != null)
                    .map(x -> new QueryHavingModel(x.getQueryHaving()))
                    .collect(Collectors.toList());
            this.querySelectModels = dtoClassInfo.getNormalFieldList()
                    .stream().filter(x -> x.getQuerySelect() != null || x.getQueryGroupBy() != null)
                    .map(x -> {
                        if(x.getQuerySelect() != null) {
                            return new QuerySelectModel(x.getQuerySelect()
                                    , this.tableName
                                    , x.getEntityFieldInfo().getColumnName());
                        } else {
                            return new QuerySelectModel(x.getQueryGroupBy()
                                    , this.tableName
                                    , x.getEntityFieldInfo().getColumnName());
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            this.queryHavingModels = new ArrayList<>();
            this.joinTableDtoFields = dtoClassInfo.getSubDtoFieldList()
                .stream()
                .filter(x -> x.getTableJoinInfo() != null)
                .collect(Collectors.toList());

            List<QuerySelectModel> joinQueryModels = this.joinTableDtoFields.stream().map(x -> {
                return this.dtoClassInfo.getDtoClassInfoHelper().getDtoClassInfo(x.getEntityDtoServiceRelation())
                        .getNormalFieldList().stream()
                        .filter(y -> y.getQuerySelect() != null || y.getEntityFieldInfo() != null)
                        .map(y ->
                        new QuerySelectModel(y.getQuerySelect()
                                , x.getTableJoinInfo().getQueryJoin().getTableAlias()
                                , y.getEntityFieldInfo().getColumnName()
                                , TableJoinColumnPrefixManager.tryGet((Class)x.getDtoClassInfo().getEntityClassInfo().getEntityClass()
                                , x.getTableJoinInfo().getActualClass()
                                , x.getTableJoinInfo().getPosition())))
                        .collect(Collectors.toList());
            }).flatMap(x -> x.stream()).collect(Collectors.toList());
            List<QuerySelectModel> querySelectModels = dtoClassInfo.getNormalFieldList()
                    .stream().filter(x -> x.getQuerySelect() != null || x.getEntityFieldInfo() != null)
                    .map(x -> {
                        return new QuerySelectModel(x.getQuerySelect()
                                , this.tableName
                                , x.getEntityFieldInfo().getColumnName());
                    })
                    .collect(Collectors.toList());
            this.querySelectModels = new ArrayList<>();
            this.querySelectModels.addAll(querySelectModels);
            this.querySelectModels.addAll(joinQueryModels);
        }
    }

    public static QueryWrapperBuilder createQueryWrapperBuilder(
            List<QuerySelectModel> querySelectModels
           ,List<QueryGroupByModel> queryGroupByModels
           ,List<QueryHavingModel> queryHavingModels
           ,List<QueryOrderModel> queryOrders) {
        List<String> columnList = querySelectModels.stream().map(x -> x.getExpression()).collect(Collectors.toList());
        List<String> groupByList = queryGroupByModels.stream().map(x -> x.getExpression()).collect(Collectors.toList());
        List<String> havingList = queryHavingModels.stream().map(x -> x.getExpression()).collect(Collectors.toList());
        List<QueryOrderModel> orderByList = queryOrders.stream().sorted(Comparator.comparingInt(QueryOrderModel::getSortIndex))
                .collect(Collectors.toList());

        return new QueryWrapperBuilder(columnList.toArray(new String[0])
                ,groupByList
                , havingList.stream().collect(Collectors.joining(" and "))
                , orderByList);

    }
    public static <T> QueryWrapper<T> createQueryWrapper(Object dto, List<QueryWhereModel> queryWhereModels, QueryWrapperBuilder queryWrapperBuilder){
        QueryWrapper<T> result = queryWrapperBuilder.createQueryWrapper();
        for (QueryWhereModel queryWhere : queryWhereModels) {
            String expression = queryWhere.getExpression();
            if(Connector.CUSTOM.equals(queryWhere.getConnector())
                        || Connector.EXISTS.equals(queryWhere.getConnector())
                        || Connector.NOT_EXISTS.equals(queryWhere.getConnector())) {
                expression = SPELParser.parseStringExpression(dto, expression);
            }
            String tableColumn = queryWhere.getTableColumn();
            if(queryWhere.isDynamicTableColumn()) {
                tableColumn = SPELParser.parseStringExpression(dto, tableColumn);
            }
            queryWhere.getConnector().consume(tableColumn
                    , result
                    , StringUtils.hasLength(expression) ? expression :
                    FieldUtils.getFieldValue(dto, queryWhere.getDtoField().getPropertyDescriptor().getReadMethod()));
        }
        return result;
    }

    public <T> QueryWrapper<T> createQueryWrapper(Object dto, List<QueryWhereModel> queryWhereModels) {
        if(this.baseWrapperBuilder == null) {
            this.baseWrapperBuilder = createQueryWrapperBuilder(this.querySelectModels
                    , this.queryGroupByModels
                    , this.queryHavingModels
                    , this.queryOrders);
        }
        return createQueryWrapper(dto, queryWhereModels, this.baseWrapperBuilder);
    }

    public <T> QueryWrapper<T> createQueryWrapper(Object dto) {
        return createQueryWrapper(dto, this.queryWhereModels);
    }

    public static class QueryWrapperBuilder {
        private final String[] columns;
        private final List<String> groupBys;
        private final String having;
        private final List<QueryOrderModel> orderModels;

        private QueryWrapperBuilder(String[] columns, List<String> groupBys, String having
                , List<QueryOrderModel> orderModels) {
            this.columns = columns;
            this.groupBys = groupBys;
            this.having = having;
            this.orderModels = orderModels;
        }

        public <T> QueryWrapper<T> createQueryWrapper() {
            QueryWrapper<T> queryWrapper = new QueryWrapper<>();
            if(columns != null && columns.length > 0) {
                queryWrapper.select(columns);
            }
            if(groupBys != null && !groupBys.isEmpty()) {
                queryWrapper.groupBy(groupBys);
            }
            if(!StringUtils.isEmpty(having)) {
                queryWrapper.having(having);
            }
            orderModels.stream().forEach(x ->  {
                if(QueryOrder.ORDER_DESC.equals(x.getSortOrder())) {
                    queryWrapper.orderByDesc(x.getOrderExpression());
                } else {
                    queryWrapper.orderByAsc(x.getOrderExpression());
                }
            });

            return queryWrapper;
        }
    }
}
