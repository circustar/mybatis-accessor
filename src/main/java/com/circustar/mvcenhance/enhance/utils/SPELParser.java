package com.circustar.mvcenhance.enhance.utils;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.stream.Collectors;

public class SPELParser {
    public static ExpressionParser expressionParser = new SpelExpressionParser();

    public static Object parseExpression(Object obj, String expressionString) {
        StandardEvaluationContext context = new StandardEvaluationContext(obj);
        Expression expression = expressionParser.parseExpression(expressionString);
        return expression.getValue(context);
    }

    public static Object parseExpression(StandardEvaluationContext context, String expressionString) {
        Expression expression = expressionParser.parseExpression(expressionString);
        return expression.getValue(context);
    }

    public static List<Object> parseExpression(StandardEvaluationContext context, List<String> expressionStrings) {
        return expressionStrings.stream().map(x -> parseExpression(context, x)).collect(Collectors.toList());
    }
}
