package com.circustar.mvcenhance.common.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public enum JoinConnector {
    eq("eq", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " = " + values[0];
    }),
    isNull("isNull", (column, values) -> {
        return column + " is null ";
    }),
    isNotNull("isNotNull", (column, values) -> {
        return column + " is not null ";
    }),
    like("like", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " like %" + values[0] + "%";
    }),
    likeLeft("likeLeft", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " like " + values[0] + "%";
    }),
    likeRight("likeRight", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " like %" + values[0];
    }),
    in("in", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " in (" + Arrays.stream(values).collect(Collectors.joining(","))  + ") ";
    }),
    gt("gt", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " > " + values[0];
    }),
    ge("ge", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " >= " + values[0];
    }),
    lt("lt", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " < " + values[0];
    }),
    le("le", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " <= " + values[0];
    }),
    between("between", (column, values) -> {
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
    exists("exists", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return " exists( " + values[0] + ")";
    }),
    ne("ne", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " <> " + values[0];
    }),
    notLike("notLike", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " not like %" + values[0] + "%";
    }),
    notIn("notIn", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return column + " not in (" + Arrays.stream(values).collect(Collectors.joining(",")) + ") ";
    }),
    notBetween("notBetween", (column, values) -> {
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
    notExists("notExists", (column, values) -> {
        if (values == null || values.length == 0) {
            return "";
        }
        return " not exists( " + values[0] + ")";
    })
    ;
    private String connector;
    private BiFunction<String, String[], String> func;

    JoinConnector(String connector, BiFunction<String, String[], String> func) {
        this.connector = connector;
        this.func = func;
    }

    public String convert(String column, String[] values) {
        return func.apply(column, values);
    }

    public static JoinConnector getConnectorByName(String name) {
        if(StringUtils.isEmpty(name)) return null;
//        String lowerName = (name == null?null : name.toLowerCase());
        return Arrays.stream(JoinConnector.values()).filter(x -> x.connector.equals(name)).findFirst().orElse(null);
    };
    
}
