package com.circustar.common_utils.parser;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.stream.Collectors;

public class SPELParser {
    public final static ExpressionParser expressionParser = new SpelExpressionParser();

    private final static ParserContext parserContext = new TemplateParserContext();

    public static <T> T calcExpression(String expressionString, Class<T> clazz) {
        Expression expression = expressionParser.parseExpression(expressionString);
        return expression.getValue(clazz);
    }

    public static <T> T calcExpression(Object obj, String expressionString, Class<T> clazz) {
        StandardEvaluationContext context = new StandardEvaluationContext(obj);
        Expression expression = expressionParser.parseExpression(expressionString);
        return expression.getValue(context, clazz);
    }


    public static Object parseExpression(Object obj, String expressionString) {
        StandardEvaluationContext context = new StandardEvaluationContext(obj);
        context.addPropertyAccessor(new MapAccessor());
        return parseExpression(context, expressionString);
    }

    public static Object parseExpression(StandardEvaluationContext context, String expressionString) {
        Expression expression = expressionParser.parseExpression(expressionString, parserContext);
        return expression.getValue(context);
    }

    public static List<Object> parseExpression(Object object, List<String> expressionStrings) {
        return expressionStrings.stream().map(x -> parseExpression(object, x)).collect(Collectors.toList());
    }

    public static List<Object> parseExpression(StandardEvaluationContext context, List<String> expressionStrings) {
        return expressionStrings.stream().map(x -> parseExpression(context, x)).collect(Collectors.toList());
    }
}
