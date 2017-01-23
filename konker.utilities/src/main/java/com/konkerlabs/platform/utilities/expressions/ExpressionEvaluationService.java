package com.konkerlabs.platform.utilities.expressions;

import java.util.Map;
import java.util.regex.Pattern;

public interface ExpressionEvaluationService {

    Pattern EXPRESSION_TEMPLATE_PATTERN = Pattern.compile(".*\\@\\{.*}.*");

    boolean evaluateConditional(String conditionalExpression, Map<String, Object> evaluationContext);
    String evaluateTemplate(String expressionTemplate, Map<String, Object> evaluationContext);

}
