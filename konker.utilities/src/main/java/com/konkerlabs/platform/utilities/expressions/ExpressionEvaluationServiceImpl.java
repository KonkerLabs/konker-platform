package com.konkerlabs.platform.utilities.expressions;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExpressionEvaluationServiceImpl implements ExpressionEvaluationService {

    @Override
    public String evaluateTemplate(String expressionTemplate, Map<String, Object> evaluationContext) {
        Optional.ofNullable(expressionTemplate)
            .filter(template -> !template.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException("Expression template cannot be null or empty"));

        Optional.ofNullable(evaluationContext)
            .orElseThrow(() -> new IllegalArgumentException("Evaluation context cannot be null"));

        Expression expression = new SpelExpressionParser().parseExpression(expressionTemplate
                ,new TemplateParserContext("@{","}"));

        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext();
        standardEvaluationContext.addPropertyAccessor(new MapAccessor());
        standardEvaluationContext.setVariables(evaluationContext);

        return expression.getValue(standardEvaluationContext,String.class);
    }

    @Override
    public boolean evaluateConditional(String conditionalExpression, Map<String, Object> evaluationContext) {
        Optional.ofNullable(conditionalExpression)
                .filter(template -> !template.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Conditional expression cannot be null or empty"));

        Optional.ofNullable(evaluationContext)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation context cannot be null"));

        Expression expression = new SpelExpressionParser().parseExpression(conditionalExpression);

        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext();
        standardEvaluationContext.addPropertyAccessor(new MapAccessor());
        standardEvaluationContext.setVariables(evaluationContext);

        return expression.getValue(standardEvaluationContext,Boolean.class);
    }
}
