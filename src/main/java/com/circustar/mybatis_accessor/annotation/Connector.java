package com.circustar.mybatis_accessor.annotation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public enum Connector {
    EQ("eq", (column, wrapper, values) -> {
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
    IS_NULL("isNull", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){
            wrapper.isNull(column);
        }
    }, (column, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){
            return column + " is null ";
        }
        return "";
    }),
    LIKE("like", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.like(column, values[0]);}
        ,(column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " like %" + values[0] + "%";
    }),
    LIKE_LEFT("likeLeft", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.likeLeft(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " like " + values[0] + "%";
    }),
    LIKE_RIGHT("likeRight", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.likeRight(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " like %" + values[0];
    }),
    IN("in", (column, wrapper, values) -> {
        if(values == null || values.length == 0){return;}
        wrapper.in(column, values);}
        , (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " in (" + Arrays.stream(values).collect(Collectors.joining(","))  + ") ";
    }),
    GT("gt", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.gt(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " > " + values[0];
    }),
    GE("ge", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.ge(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " >= " + values[0];
    }),
    LT("lt", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.lt(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " < " + values[0];
    }),
    LE("le", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.le(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " <= " + values[0];
    }),
    BETWEEN("between", (column, wrapper, values) -> {
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
    EXISTS("exists", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.exists(values[0].toString());
    }, (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return " exists( " + values[0] + ")";
    }),
    NE("ne", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.ne(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " <> " + values[0];
    }),
    IS_NOT_NULL("isNotNull", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){wrapper.isNotNull(column);}
    }, (column, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return column + " is not null ";}
        return "";
    }),
    NOT_LIKE("notLike", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.notLike(column, values[0]);}
        , (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return column + " not like %" + values[0] + "%";
    }),
    NOT_IN("notIn", (column, wrapper, values) -> {
        if(values == null || values.length == 0){return;}
        wrapper.notIn(column, values);}
        , (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " not in (" + Arrays.stream(values).collect(Collectors.joining(",")) + ") ";
    }),
    NOT_BETWEEN("notBetween", (column, wrapper, values) -> {
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
    NOT_EXISTS("notExists", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.notExists(values[0].toString());
    }, (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return " not exists( " + values[0] + ")";
    }),
    CUSTOM("custom", (column, wrapper, values) -> {
        if(values == null || values.length == 0 || StringUtils.isEmpty(values[0])){return;}
        wrapper.notExists(values[0].toString());
    }, (column, values) -> {
        if (values == null || values.length == 0 || StringUtils.isEmpty(values[0])) {
            return "";
        }
        return " ( " + values[0] + ")";
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

    public <T> void consume(String column, QueryWrapper wrapper, T... values) {
        consumer.accept(column, wrapper, values);
    }

    public <T> void consume(String column, QueryWrapper wrapper, T value) {
        consumer.accept(column, wrapper, (value instanceof List ? (List)value : Arrays.asList(value)).toArray());
    }

    public String convert(String column, String... values) {
        return func.apply(column, values);
    }

    public static Connector getConnectorByName(String name) {
        if(StringUtils.isEmpty(name)) {return null;}
        return Arrays.stream(Connector.values()).filter(x -> x.connector.equals(name)).findFirst().orElse(null);
    };

}
