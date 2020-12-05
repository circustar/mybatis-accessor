package com.circustar.mvcenhance.common.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public enum Connector {
    eq("eq", (column, wrapper, values) -> {
        if(values == null || values.size() == 0 || StringUtils.isEmpty(values.get(0))){return;}
        wrapper.eq(column, values.get(0));}),
    isNull("isNull", (column, wrapper, values) -> {
        wrapper.isNull(column);}),
    isNotNull("isNotNull", (column, wrapper, values) -> {
        wrapper.isNotNull(column);}),
    like("like", (column, wrapper, values) -> {
        if(values == null || values.size() == 0 || StringUtils.isEmpty(values.get(0))){return;}
        wrapper.like(column, values.get(0));}),
    likeLeft("likeLeft", (column, wrapper, values) -> {
        if(values == null || values.size() == 0 || StringUtils.isEmpty(values.get(0))){return;}
        wrapper.likeRight(column, values.get(0));}),
    likeRight("likeRight", (column, wrapper, values) -> {
        if(values == null || values.size() == 0 || StringUtils.isEmpty(values.get(0))){return;}
        wrapper.likeLeft(column, values.get(0));}),
    in("in", (column, wrapper, values) -> {
        if(values == null || values.size() == 0){return;}
        wrapper.in(column, values);}),
    gt("gt", (column, wrapper, values) -> {
        if(values == null || values.size() == 0 || StringUtils.isEmpty(values.get(0))){return;}
        wrapper.gt(column, values.get(0));}),
    ge("ge", (column, wrapper, values) -> {
        if(values == null || values.size() == 0 || StringUtils.isEmpty(values.get(0))){return;}
        wrapper.ge(column, values.get(0));}),
    lt("lt", (column, wrapper, values) -> {
        if(values == null || values.size() == 0 || StringUtils.isEmpty(values.get(0))){return;}
        wrapper.lt(column, values.get(0));}),
    le("le", (column, wrapper, values) -> {
        if(values == null || values.size() == 0 || StringUtils.isEmpty(values.get(0))){return;}
        wrapper.le(column, values.get(0));}),
    between("between", (column, wrapper, values) -> {
        if(values != null && values.size() >= 2) {
            if(!StringUtils.isEmpty(values.get(0)) && !StringUtils.isEmpty(values.get(1))) {
                wrapper.between(column,values.get(0), values.get(1));
            } else if(!StringUtils.isEmpty(values.get(0))) {
                wrapper.ge(column, values.get(0));
            } else if(!StringUtils.isEmpty(values.get(1))) {
                wrapper.le(column, values.get(1));
            }
        }
    }),
    exists("exists", (column, wrapper, values) -> {
        if(values == null || values.size() == 0 || StringUtils.isEmpty(values.get(0))){return;}
        wrapper.exists(values.get(0).toString());
    }),
    ne("ne", (column, wrapper, values) -> {
        if(values == null || values.size() == 0 || StringUtils.isEmpty(values.get(0))){return;}
        wrapper.ne(column, values.get(0));}),
    notLike("notLike", (column, wrapper, values) -> {
        if(values == null || values.size() == 0 || StringUtils.isEmpty(values.get(0))){return;}
        wrapper.notLike(column, values.get(0));}),
    notIn("notIn", (column, wrapper, values) -> {
        if(values == null || values.size() == 0){return;}
        wrapper.notIn(column, values);}),
    notBetween("notBetween", (column, wrapper, values) -> {
        if(values == null){return;}
        if(values.size() >= 2) {
            if(!StringUtils.isEmpty(values.get(0)) && !StringUtils.isEmpty(values.get(1))) {
                wrapper.notBetween(column,values.get(0), values.get(1));
            } else if(!StringUtils.isEmpty(values.get(0))) {
                wrapper.lt(column, values.get(0));
            } else if(!StringUtils.isEmpty(values.get(1))) {
                wrapper.gt(column, values.get(1));
            }
        }
    }),
    notExists("notExists", (column, wrapper, values) -> {
        if(values == null || values.size() == 0 || StringUtils.isEmpty(values.get(0))){return;}
        wrapper.notExists(values.get(0).toString());
    })
    ;
    private String connector;
    private TriConsumer<String, QueryWrapper, List> consumer;

    Connector(String connector, TriConsumer<String, QueryWrapper, List> consumer) {
        this.connector = connector;
        this.consumer = consumer;
    }

    public <T> void consume(String column, QueryWrapper wrapper, List<T> values) {
//        if (values == null || values.size() == 0) {
//            return;
//        }
        consumer.accept(column, wrapper, values);
    }

    public static Connector getConnectorByName(String name) {
        if(StringUtils.isEmpty(name)) return null;
//        String lowerName = (name == null?null : name.toLowerCase());
        return Arrays.stream(Connector.values()).filter(x -> x.connector.equals(name)).findFirst().orElse(null);
    };

}
