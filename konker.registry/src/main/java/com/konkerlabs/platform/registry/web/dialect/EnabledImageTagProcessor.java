package com.konkerlabs.platform.registry.web.dialect;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

public class EnabledImageTagProcessor extends AbstractAttributeTagProcessor {

	private static final String ATTR_NAME = "enabled-img";
	private static final int PRECEDENCE = 10000;

	public EnabledImageTagProcessor(final String dialectPrefix) {
		super(TemplateMode.HTML, // This processor will apply only to HTML mode
				dialectPrefix, // Prefix to be applied to name for matching
				null, // No tag name: match any tag name
				false, // No prefix to be applied to tag name
				ATTR_NAME, // Name of the attribute that will be matched
				true, // Apply dialect prefix to attribute name
				PRECEDENCE, // Precedence (inside dialect's precedence)
				true); // Remove the matched attribute afterwards
	}

	protected void doProcess(final ITemplateContext context, final IProcessableElementTag tag,
			final AttributeName attributeName, final String attributeValue,
			final IElementTagStructureHandler structureHandler) {

		IStandardExpressionParser expressionParser = StandardExpressions
				.getExpressionParser(context.getConfiguration());

		IStandardExpression expression = expressionParser.parseExpression(context, attributeValue);
		Boolean enabled = (Boolean) expression.execute(context);

		if (enabled) {
			expression = expressionParser.parseExpression(context, "@{/resources/konker/images/enabled.svg}");
		} else {
			expression = expressionParser.parseExpression(context, "@{/resources/konker/images/disabled.svg}");
		}

		structureHandler.setAttribute("src", (String) expression.execute(context));
		structureHandler.setAttribute("class", (String) "enabled-img");

	}

}