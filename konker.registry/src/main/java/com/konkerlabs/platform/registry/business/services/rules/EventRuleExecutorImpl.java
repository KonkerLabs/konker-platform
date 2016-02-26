package com.konkerlabs.platform.registry.business.services.rules;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleExecutor;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRulePublisher;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.json.JsonPropertyAccessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventRuleExecutorImpl implements EventRuleExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventRuleExecutorImpl.class);

    @Autowired
    private EventRuleService eventRuleService;

    @Autowired
    private ApplicationContext applicationContext;

    private EventRulePublisher eventRulePublisher;

    public enum RuleTransformationType {EXPRESSION_LANGUAGE}

    @Async
    @Override
    public Future<List<Event>> execute(Event event, URI uri) {
        List<EventRule> eventRules = eventRuleService.findByIncomingUri(uri);
        String incomingPayload = event.getPayload();

        List<Event> outEvents = new ArrayList<Event>();

        //FIXME execute event rules in parallel
        for (EventRule eventRule : eventRules) {
            if (!eventRule.isActive())
                continue;
            if (!eventRule.getIncoming().getData().get("channel").equals(event.getChannel())) {
                LOGGER.debug("Non matching channel for incoming event: {}", event);
                continue;
            }

            for (EventRule.RuleTransformation ruleTransformation : eventRule.getTransformations()) {
                switch (RuleTransformationType.valueOf(ruleTransformation.getType())) {
                    case EXPRESSION_LANGUAGE:

                    Optional.ofNullable(ruleTransformation.getData().get("value"))
                        .filter(filter -> !filter.isEmpty())
                        .ifPresent(filter -> {
                            Expression expression = null;
                            StandardEvaluationContext standardEvaluationContext = null;

                            try {
                                expression = new SpelExpressionParser().parseExpression(filter);

                                standardEvaluationContext = new StandardEvaluationContext();
                                standardEvaluationContext.setVariables(
                                    new ObjectMapper().readValue(incomingPayload,
                                        new TypeReference<Map<String, Object>>() {
                                        })
                                );
                                standardEvaluationContext.addPropertyAccessor(new MapAccessor());

                                if (expression.getValue(standardEvaluationContext, Boolean.class)) {
                                    eventRulePublisher = (EventRulePublisher) applicationContext.getBean(eventRule.getOutgoing().getUri().getScheme());
                                    Event outEvent = Event.builder().timestamp(Instant.now()).payload(incomingPayload).build();
                                    eventRulePublisher.send(outEvent, eventRule.getOutgoing());
                                    outEvents.add(outEvent);
                                } else {
                                    LOGGER.debug(MessageFormat.format("Dropped rule \"{0}\", not matching \"{1}\" pattern with content \"{2}\". Message payload: {3} ",
                                            eventRule.getName(), ruleTransformation.getType(), filter, incomingPayload));
                                }
                            } catch (IOException e) {
                                LOGGER.error("Error parsing JSON payload.", e);
                            } catch (SpelEvaluationException e) {
                                LOGGER.error(MessageFormat
                                    .format("Error evaluating, probably malformed, expression: \"{0}\". Message payload: {1} ",
                                            filter,
                                            incomingPayload), e);
                            }
                        });

                        break;
                }
            }
        }
        return new AsyncResult<List<Event>>(outEvents);
    }

}
