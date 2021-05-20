package com.circustar.common_utils.parser;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;

public class SPELParser {
    public static ExpressionParser expressionParser = new SpelExpressionParser();

    private static ParserContext parserContext = new TemplateParserContext();

    public static Object parseExpression(Object obj, String expressionString) {
        StandardEvaluationContext context = new StandardEvaluationContext(obj);
        context.addPropertyAccessor(new MapAccessor());
        return parseExpression(context, expressionString);
    }

    public static Object parseExpression(StandardEvaluationContext context, String expressionString) {
        Expression expression = expressionParser.parseExpression(expressionString, parserContext);
        return expression.getValue(context);
    }

    public static Object[] parseExpression(Object object, List<String> expressionStrings) {
        return expressionStrings.stream().map(x -> parseExpression(object, x)).toArray();
    }

    public static Object[] parseExpression(StandardEvaluationContext context, List<String> expressionStrings) {
        return expressionStrings.stream().map(x -> parseExpression(context, x)).toArray();
    }
}
