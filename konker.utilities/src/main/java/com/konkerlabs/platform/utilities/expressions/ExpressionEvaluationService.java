package com.konkerlabs.platform.utilities.expressions;

import java.util.Map;
import java.util.regex.Pattern;

public interface ExpressionEvaluationService {

    Pattern EXPRESSION_TEMPLATE_PATTERN = Pattern.compile(".*\\@\\{.*}.*");

    String evaluateTemplate(String expressionTemplate, Map<String, Object> evaluationContext);
    boolean evaluateConditional(String conditionalExpression, Map<String, Object> evaluationContext);

}
