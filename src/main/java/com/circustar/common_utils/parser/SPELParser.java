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

public abstract class SPELParser {
    public final static ExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

    private final static ParserContext TEMPLATE_PARSER_CONTEXT = new TemplateParserContext();

    public static <T> T calcExpression(String expressionString, final Class<T> clazz) {
        Expression expression = SPEL_EXPRESSION_PARSER.parseExpression(expressionString);
        return expression.getValue(clazz);
    }

    public static <T> T calcExpression(Object obj, String expressionString, Class<T> clazz) {
        StandardEvaluationContext context = new StandardEvaluationContext(obj);
        Expression expression = SPEL_EXPRESSION_PARSER.parseExpression(expressionString);
        return expression.getValue(context, clazz);
    }


    public static Object parseExpression(Object obj, String expressionString) {
        StandardEvaluationContext context = new StandardEvaluationContext(obj);
        context.addPropertyAccessor(new MapAccessor());
        return parseExpression(context, expressionString);
    }

    public static Object parseExpression(StandardEvaluationContext context, String expressionString) {
        Expression expression = SPEL_EXPRESSION_PARSER.parseExpression(expressionString, TEMPLATE_PARSER_CONTEXT);
        return expression.getValue(context);
    }

    public static List<Object> parseExpression(Object object, List<String> expressionStrings) {
        return expressionStrings.stream().map(x -> parseExpression(object, x)).collect(Collectors.toList());
    }

    public static List<Object> parseExpression(StandardEvaluationContext context, List<String> expressionStrings) {
        return expressionStrings.stream().map(x -> parseExpression(context, x)).collect(Collectors.toList());
    }
}
