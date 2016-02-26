package com.konkerlabs.platform.utilities.expressions;

import java.util.Map;

public interface ExpressionEvaluationService {

    String evaluateTemplate(String expressionTemplate, Map<String, Object> evaluationContext);
    boolean evaluateConditional(String conditionalExpression, Map<String, Object> evaluationContext);

}
