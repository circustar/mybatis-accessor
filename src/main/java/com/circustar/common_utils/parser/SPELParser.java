package com.circustar.common_utils.parser;

import org.springframework.context.expression.MapAccessor;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.*;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectiveMethodExecutor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SPELParser {
    public final static ExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

    private final static ParserContext TEMPLATE_PARSER_CONTEXT = new TemplateParserContext();
    private final static String METHOD_NAME_CTL = "c2l";
    private final static MethodResolver methodResolver;

    static {
        try {
            methodResolver = new MethodResolver() {
                private final Method method = com.circustar.common_utils.collection.StringUtils.class.getDeclaredMethod(METHOD_NAME_CTL, String.class);
                @Override
                public MethodExecutor resolve(EvaluationContext evaluationContext, Object o, String s, List<TypeDescriptor> list) throws AccessException {
                    if(METHOD_NAME_CTL.equals(s)) {
                        return new ReflectiveMethodExecutor(method);
                    }
                    return null;
                }
            };
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final static String PARSER_SIGNAL_START = "#{";
    private final static String PARSER_SIGNAL_END = "}";

    public static <T> T calcExpression(String expressionString, final Class<T> clazz) {
        Expression expression = SPEL_EXPRESSION_PARSER.parseExpression(expressionString);
        return expression.getValue(clazz);
    }

    public static <T> T calcExpression(Object obj, String expressionString, Class<T> clazz) {
        StandardEvaluationContext context = new StandardEvaluationContext(obj);
        Expression expression = SPEL_EXPRESSION_PARSER.parseExpression(expressionString);
        return expression.getValue(context, clazz);
    }

    public static boolean parseBooleanExpression(Object obj, String expressionString, boolean defaultOnEmpty) {
        if(obj == null || !StringUtils.hasLength(expressionString)) {
           return defaultOnEmpty;
        }
        String strExpression;
        if(expressionString.contains(PARSER_SIGNAL_START)) {
            strExpression = expressionString;
        } else {
            strExpression = PARSER_SIGNAL_START + expressionString + PARSER_SIGNAL_END;
        }
        return (boolean)parseExpression(obj, strExpression, defaultOnEmpty);
    }

    public static String parseStringExpression(Object obj, String expressionString) {
        return (String)parseExpression(obj, expressionString, expressionString);
    }

    public static Object parseExpression(Object obj, String expressionString, Object defaultValue) {
        if(obj == null || !StringUtils.hasLength(expressionString) || !expressionString.contains(PARSER_SIGNAL_START)) {
            return defaultValue;
        }
        StandardEvaluationContext context = new StandardEvaluationContext(obj);
        context.addPropertyAccessor(new MapAccessor());
        return parseExpression(context, expressionString);
    }


    public static Object parseExpression(StandardEvaluationContext context, String expressionString) {
        context.addMethodResolver(methodResolver);
        Expression expression = SPEL_EXPRESSION_PARSER.parseExpression(expressionString, TEMPLATE_PARSER_CONTEXT);
        return expression.getValue(context);
    }

    public static List<Object> parseExpressionList(Object object, List<String> expressionStrings) {
        if(object == null) {
            return null;
        }
        StandardEvaluationContext context = new StandardEvaluationContext(object);
        context.addPropertyAccessor(new MapAccessor());
        return expressionStrings.stream().map(x -> parseExpression(context, x)).collect(Collectors.toList());
    }
}
