package com.circustar.mvcenhance.wrapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.circustar.mvcenhance.annotation.QueryOrder;
import com.circustar.mvcenhance.classInfo.DtoClassInfo;
import com.circustar.mvcenhance.classInfo.DtoField;
import com.circustar.mvcenhance.classInfo.EntityClassInfo;
import com.circustar.mvcenhance.classInfo.TableFieldInfo;
import com.circustar.mvcenhance.utils.FieldUtils;
import com.circustar.mvcenhance.utils.TableInfoUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class QueryWrapperCreator {
    private String tableName;
    private List<QuerySelectModel> querySelectModels;
    private List<QueryJoinModel> queryJoinModels;
    private List<QueryWhereModel> queryWhereModels;
    private List<QueryGroupByModel> queryGroupByModels;
    private List<QueryHavingModel> queryHavingModels;
    private List<QueryOrderModel> queryOrders;
    private DtoClassInfo dtoClassInfo;
    private EntityClassInfo entityClassInfo;
    private TableInfo tableInfo;
    private List<DtoClassInfo> joinTableDtoClassList = new ArrayList<>();

    public QueryWrapperCreator(DtoClassInfo dtoClassInfo) {
        this.dtoClassInfo = dtoClassInfo;
        this.entityClassInfo = dtoClassInfo.getEntityClassInfo();
        this.tableInfo = entityClassInfo.getTableInfo();
        this.tableName = this.tableInfo.getTableName();
        this.queryGroupByModels = dtoClassInfo.getNormalFieldList()
                .stream().filter(x -> x.getQueryGroupBy() != null)
                .map(x -> new QueryGroupByModel(x.getQueryGroupBy()))
                .collect(Collectors.toList());

        this.queryJoinModels = dtoClassInfo.getSubDtoFieldList()
                .stream()
                .map(x -> {
                    List<DtoField> normalFieldList = x.getDtoClassInfo().getNormalFieldList();
                    String thisTableId = this.tableInfo.getKeyColumn();
                    String thatTableId = null;
                    EntityClassInfo thatEntityClassInfo = x.getDtoClassInfo().getEntityClassInfo();
                    for(TableFieldInfo tableFieldInfo : thatEntityClassInfo.getFieldList()) {
                        if(thisTableId.equals(tableFieldInfo.getColumnName())) {
                            thatTableId = tableFieldInfo.getColumnName();
                            break;
                        }
                    }
                    if(thatTableId == null) {
                        thisTableId = null;
                        thatTableId = thatEntityClassInfo.getTableInfo().getKeyColumn();
                        for(TableFieldInfo tableFieldInfo : this.entityClassInfo.getFieldList()) {
                            if(thatTableId.equals(tableFieldInfo.getColumnName())) {
                                thisTableId = tableFieldInfo.getColumnName();
                            }
                        }
                    }
                    if(StringUtils.isEmpty(thisTableId) || StringUtils.isEmpty(thatTableId)) {
                        return null;
                    }
                    joinTableDtoClassList.add(x.getDtoClassInfo());
                    return new QueryJoinModel(x.getQueryJoin()
                        , this.tableName, thisTableId
                        , x.getDtoClassInfo().getEntityClassInfo().getTableInfo().getTableName()
                        , thatTableId);
                }).filter(x -> x != null)
                .sorted(Comparator.comparingInt(QueryJoinModel::getOrder))
                .collect(Collectors.toList());
        this.queryWhereModels = dtoClassInfo.getNormalFieldList()
                .stream().map(x -> new QueryWhereModel(x.getQueryWhere()
                        , this.tableName, x.getTableFieldInfo()))
                .collect(Collectors.toList());
        this.queryOrders = dtoClassInfo.getNormalFieldList()
                .stream().filter(x -> x.getQueryOrder() != null)
                .map(x -> new QueryOrderModel(x.getQueryOrder()))
                .sorted(Comparator.comparingInt(QueryOrderModel::getSortIndex))
                .collect(Collectors.toList());

        if(this.queryJoinModels.size() > 0) {
            this.queryHavingModels = dtoClassInfo.getNormalFieldList()
                    .stream().filter(x -> x.getQueryHaving() != null)
                    .map(x -> new QueryHavingModel(x.getQueryHaving()))
                    .collect(Collectors.toList());
            this.querySelectModels = dtoClassInfo.getNormalFieldList()
                    .stream().filter(x -> x.getQuerySelect() != null || x.getQueryGroupBy() != null)
                    .map(x -> {
                        if(x.getQuerySelect() != null) {
                            return new QuerySelectModel(x.getQuerySelect()
                                    , this.tableName
                                    , x.getTableFieldInfo().getColumnName());
                        } else {
                            return new QuerySelectModel(x.getQueryGroupBy()
                                    , this.tableName
                                    , x.getTableFieldInfo().getColumnName());
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            List<QuerySelectModel> joinQueryModels = this.joinTableDtoClassList.stream().map(x -> {
                return x.getNormalFieldList().stream().map(y ->
                        new QuerySelectModel(y.getQuerySelect()
                                , x.getEntityClassInfo().getTableInfo().getTableName()
                                , y.getTableFieldInfo().getColumnName()
                                , x.getEntityClassInfo().getTableInfo().getTableName()))
                        .collect(Collectors.toList());
            }).flatMap(x -> x.stream()).collect(Collectors.toList());
            List<QuerySelectModel> querySelectModels = dtoClassInfo.getNormalFieldList()
                    .stream()
                    .map(x -> {
                        return new QuerySelectModel(x.getQuerySelect()
                                , this.tableName
                                , x.getTableFieldInfo().getColumnName());
                    })
                    .collect(Collectors.toList());
            this.querySelectModels = new ArrayList<>();
            this.querySelectModels.addAll(querySelectModels);
            this.querySelectModels.addAll(joinQueryModels);
        }
    }

    private QueryWrapperBuilder baseWrapperBuilder = null;
    public static QueryWrapperBuilder createQueryWrapperBuilder(
            List<QuerySelectModel> querySelectModels
           ,List<QueryGroupByModel> queryGroupByModels
           ,List<QueryHavingModel> queryHavingModels
           ,List<QueryOrderModel> queryOrders) {
        List<String> columnList = querySelectModels.stream().map(x -> x.getExpression()).collect(Collectors.toList());
        List<String> groupByList = queryGroupByModels.stream().map(x -> x.getExpression()).collect(Collectors.toList());
        List<String> havingList = queryHavingModels.stream().map(x -> x.getExpression()).collect(Collectors.toList());
        List<QueryOrderModel> orderByDescList = queryOrders.stream().filter(x -> QueryOrder.ORDER_DESC.equals(x.getSortOrder())).collect(Collectors.toList());
        List<QueryOrderModel> orderByAscList = queryOrders.stream().filter(x -> !QueryOrder.ORDER_DESC.equals(x.getSortOrder())).collect(Collectors.toList());

        return new QueryWrapperBuilder(columnList.toArray(new String[columnList.size()])
                ,groupByList.toArray(new String[groupByList.size()])
                , havingList.stream().collect(Collectors.joining(","))
                , orderByAscList.toArray(new String[orderByAscList.size()])
                , orderByDescList.toArray(new String[orderByDescList.size()]));

    }
    public static <T> QueryWrapper<T> createQueryWrapper(Object dto, List<QueryWhereModel> queryWhereModels, QueryWrapperBuilder queryWrapperBuilder) throws IllegalAccessException {
        QueryWrapper<T> result = queryWrapperBuilder.createQueryWrapper();
        for(QueryWhereModel queryWhere : queryWhereModels) {
            queryWhere.getConnector().consume(queryWhere.getExpression()
                    , result, FieldUtils.getValue(dto, queryWhere.getTableFieldInfo().getField()));
        }
        return result;
    }

    public <T> QueryWrapper<T> createQueryWrapper(Object dto, List<QueryWhereModel> queryWhereModels) throws IllegalAccessException {
        if(baseWrapperBuilder == null) {
            this.baseWrapperBuilder = createQueryWrapperBuilder(this.querySelectModels
                    , this.queryGroupByModels
                    , this.queryHavingModels
                    , this.queryOrders);
        }
        return createQueryWrapper(dto, queryWhereModels, this.baseWrapperBuilder);
    }

    public <T> QueryWrapper<T> createQueryWrapper(Object dto) throws IllegalAccessException {
        return createQueryWrapper(dto, this.queryWhereModels, this.baseWrapperBuilder);
    }

    public static class QueryWrapperBuilder {
        private String[] columns;
        private String[] groupBys;
        private String having;
        private String[] orderAsc;
        private String [] orderDesc;

        private QueryWrapperBuilder(String[] columns, String[] groupBys, String having, String[] orderAsc, String [] orderDesc) {
            this.columns = columns;
            this.groupBys = groupBys;
            this.having = having;
            this.orderAsc = orderAsc;
            this.orderDesc = orderDesc;
        }

        public <T> QueryWrapper<T> createQueryWrapper() {
            QueryWrapper<T> qw = new QueryWrapper<>();
            if(columns != null && columns.length > 0) {
                qw.select(columns);
            }
            if(groupBys != null && groupBys.length > 0) {
                qw.groupBy(groupBys);
            }
            if(!StringUtils.isEmpty(having)) {
                qw.having(having);
            }
            if(orderDesc != null && orderDesc.length > 0) {
                qw.orderByDesc(orderDesc);
            }
            if(orderAsc != null && orderAsc.length > 0) {
                qw.orderByAsc(orderAsc);
            }

            return qw;
        }
    }
}
