package com.circustar.mvcenhance.common.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;
import com.circustar.mvcenhance.enhance.utils.SPELParser;

import java.util.Arrays;
import java.util.Collections;

public class EntityFilterUtils {
//    public static List<TableJoiner> filterGroup(TableJoiner[] t, String groupName) {
//        return Arrays.stream(t).filter(x -> x.group().equals(groupName)).collect(Collectors.toList());
//    }

//    public static void filterGroup(Object obj, String fieldName, EntityFilter[] t, String groupName, QueryWrapper qw) throws NoSuchFieldException {
//        StandardEvaluationContext context = new StandardEvaluationContext(obj);
//
//        Arrays.stream(t).filter(x -> Arrays.stream(x.group()).anyMatch(y -> y.equals(groupName)))
//                .forEach(x -> {
//                    Object value = null;
//                    if(!StringUtils.isEmpty(x.valueExpression())) {
//                        value = SPELParser.parseExpression(context, Arrays.asList(x.valueExpression()));
//                    }
//                    x.connector().consume(x.column(), qw, Collections.singletonList(value));
//                });
//    }
}
