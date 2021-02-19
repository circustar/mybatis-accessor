package com.circustar.mvcenhance.annotation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public enum Connector {
    eq("eq", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){
            return;
        }
        wrapper.eq(column, values[0]);}
        ,(column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " = " + values[0];
    }),
    isNull("isNull", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){
            wrapper.isNull(column);
        }
    }, (column, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){
            return column + " is null ";
        }
        return "";
    }),
    like("like", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.like(column, values[0]);}
        ,(column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " like %" + values[0] + "%";
    }),
    likeLeft("likeLeft", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.likeRight(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " like " + values[0] + "%";
    }),
    likeRight("likeRight", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.likeLeft(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " like %" + values[0];
    }),
    in("in", (column, wrapper, values) -> {
        if(values == null || values.length == 0){return;}
        wrapper.in(column, values);}
        , (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " in (" + Arrays.stream(values).collect(Collectors.joining(","))  + ") ";
    }),
    gt("gt", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.gt(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " > " + values[0];
    }),
    ge("ge", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.ge(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " >= " + values[0];
    }),
    lt("lt", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.lt(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " < " + values[0];
    }),
    le("le", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.le(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " <= " + values[0];
    }),
    between("between", (column, wrapper, values) -> {
        if(values != null && values.length >= 2) {
            if(!StringUtils.isEmpty(values[0]) && !StringUtils.isEmpty(values[1])) {
                wrapper.between(column,values[0], values[1]);
            } else if(!StringUtils.isEmpty(values[0])) {
                wrapper.ge(column, values[0]);
            } else if(!StringUtils.isEmpty(values[1])) {
                wrapper.le(column, values[1]);
            }
        }
    }, (column, values) -> {
        if(values != null && values.length >= 2) {
            if(!StringUtils.isEmpty(values[0]) && !StringUtils.isEmpty(values[1])) {
                return column + " between " + values[0] + " and " + values[1];
            } else if(!StringUtils.isEmpty(values[0])) {
                return column + " >= " + values[0];
            } else if(!StringUtils.isEmpty(values[1])) {
                return column + " <= " + values[1];
            }
        }
        return "";
    }),
    exists("exists", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.exists(values[0].toString());
    }, (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return " exists( " + values[0] + ")";
    }),
    ne("ne", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.ne(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " <> " + values[0];
    }),
    isNotNull("isNotNull", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){wrapper.isNotNull(column);}
    }, (column, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return column + " is not null ";}
        return "";
    }),
    notLike("notLike", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.notLike(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " not like %" + values[0] + "%";
    }),
    notIn("notIn", (column, wrapper, values) -> {
        if(values == null || values.length == 0){return;}
        wrapper.notIn(column, values);}
        , (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " not in (" + Arrays.stream(values).collect(Collectors.joining(",")) + ") ";
    }),
    notBetween("notBetween", (column, wrapper, values) -> {
        if(values == null){return;}
        if(values.length >= 2) {
            if(!StringUtils.isEmpty(values[0]) && !StringUtils.isEmpty(values[1])) {
                wrapper.notBetween(column,values[0], values[1]);
            } else if(!StringUtils.isEmpty(values[0])) {
                wrapper.lt(column, values[0]);
            } else if(!StringUtils.isEmpty(values[1])) {
                wrapper.gt(column, values[1]);
            }
        }
    }, (column, values) -> {
        if(values != null && values.length >= 2) {
            if(!StringUtils.isEmpty(values[0]) && !StringUtils.isEmpty(values[1])) {
                return "(" + column + " < " + values[0] + " or " + column + " > " + values[1] + ")";
            } else if(!StringUtils.isEmpty(values[0])) {
                return column + " < " + values[0];
            } else if(!StringUtils.isEmpty(values[1])) {
                return column + " > " + values[1];
            }
        }
        return "";
    }),
    notExists("notExists", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.notExists(values[0].toString());
    }, (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return " not exists( " + values[0] + ")";
    })
    ;
    private String connector;
    private TriConsumer<String, QueryWrapper, Object[]> consumer;
    private BiFunction<String, String[], String> func;

    Connector(String connector, TriConsumer<String, QueryWrapper, Object[]> consumer, BiFunction<String, String[], String> func) {
        this.connector = connector;
        this.consumer = consumer;
        this.func = func;
    }

    public <T> void consume(String column, QueryWrapper wrapper, T[] values) {
        consumer.accept(column, wrapper, values);
    }

    public <T> void consume(String column, QueryWrapper wrapper, T value) {
        consumer.accept(column, wrapper, Arrays.asList(value).toArray());
    }

    public String convert(String column, String[] values) {
        return func.apply(column, values);
    }

    public static Connector getConnectorByName(String name) {
        if(StringUtils.isEmpty(name)) return null;
//        String lowerName = (name == null?null : name.toLowerCase());
        return Arrays.stream(Connector.values()).filter(x -> x.connector.equals(name)).findFirst().orElse(null);
    };

}
