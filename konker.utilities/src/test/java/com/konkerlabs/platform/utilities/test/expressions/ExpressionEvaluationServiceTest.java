package com.konkerlabs.platform.utilities.test.expressions;

import com.konkerlabs.platform.utilities.config.UtilitiesConfig;
import com.konkerlabs.platform.utilities.expressions.ExpressionEvaluationService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ExpressionEvaluationServiceTest.Template.class,
    ExpressionEvaluationServiceTest.Conditional.class
})
public class ExpressionEvaluationServiceTest {

    public static class ExpressionEvaluationServiceTestBase {

        @Rule
        public ExpectedException thrown = ExpectedException.none();

        protected Map<String, Object> rootContext;

        @Autowired
        protected ExpressionEvaluationService subject;

        @Before
        public void setUp() throws Exception {
            rootContext = new HashMap<>();

            rootContext.put("fieldOne","valueOne");
            rootContext.put("valid",true);
            Map<String, Object> complex = new HashMap<>();
            complex.put("value", 35);
            rootContext.put("complex", complex);
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes = {
        UtilitiesConfig.class
    })
    public static class Template extends ExpressionEvaluationServiceTestBase {

        private String template;

        @Override
        public void setUp() throws Exception {
            super.setUp();

            template = "http://host:8080/service/@{#fieldOne}";
        }

        @Test
        public void shouldRaiseAnExceptionIfTemplateIsNull() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Expression template cannot be null or empty");

            subject.evaluateTemplate(null,rootContext);
        }
        @Test
        public void shouldRaiseAnExceptionIfTemplateIsEmpty() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Expression template cannot be null or empty");

            subject.evaluateTemplate("",rootContext);
        }
        @Test
        public void shouldRaiseAnExceptionIfEvaluationContextIsNull() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Evaluation context cannot be null");

            subject.evaluateTemplate(template,null);
        }
        @Test
        public void shouldInterpolateTheGivenTemplate() throws Exception {
            String expected = template.replaceAll("\\@\\{.*}", rootContext.get("fieldOne").toString());

            String actual = subject.evaluateTemplate(template,rootContext);
            assertThat(actual,notNullValue());
            assertThat(actual,not(isEmptyString()));
            assertThat(actual,equalTo(expected));
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes = {
            UtilitiesConfig.class
    })
    public static class Conditional extends ExpressionEvaluationServiceTestBase {

        private String expression;

        @Override
        public void setUp() throws Exception {
            super.setUp();

            expression = "#fieldOne == 'valueOne' and #valid == true";
        }
        @Test
        public void shouldRaiseAnExceptionIfConditionalExpressionIsNull() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Conditional expression cannot be null or empty");

            subject.evaluateConditional(null,rootContext);
        }
        @Test
        public void shouldRaiseAnExceptionIfConditionalExpressionIsEmpty() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Conditional expression cannot be null or empty");

            subject.evaluateConditional("",rootContext);
        }
        @Test
        public void shouldRaiseAnExceptionIfEvaluationContextIsNull() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Evaluation context cannot be null");

            subject.evaluateConditional(expression,null);
        }
        @Test
        public void shouldEvaluateTheExpression() throws Exception {
            boolean condition = subject.evaluateConditional(expression,rootContext);

            assertThat(condition,equalTo(true));
        }
    }
}