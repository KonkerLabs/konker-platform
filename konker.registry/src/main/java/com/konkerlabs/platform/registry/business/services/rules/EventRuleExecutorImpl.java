package com.konkerlabs.platform.registry.business.services.rules;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleExecutor;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRulePublisher;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleService;
import com.konkerlabs.platform.utilities.expressions.ExpressionEvaluationService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
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
    @Autowired
    private ExpressionEvaluationService evaluationService;
    @Autowired
    private JsonParsingService jsonParsingService;

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
                    case EXPRESSION_LANGUAGE: {

                        Optional<String> expression = Optional.ofNullable(ruleTransformation.getData().get("value"))
                                .filter(filter -> !filter.isEmpty());

                        if (expression.isPresent()) {
                            try {
                                Map<String, Object> objectMap = jsonParsingService.toMap(incomingPayload);

                                if (evaluationService.evaluateConditional(expression.get(), objectMap)) {
                                    forwardEvent(eventRule.getOutgoing(), event);
                                    outEvents.add(event);
                                } else {
                                    LOGGER.debug(MessageFormat.format("Dropped rule \"{0}\", not matching \"{1}\" pattern with content \"{2}\". Message payload: {3} ",
                                            eventRule.getName(), ruleTransformation.getType(), expression.get(), incomingPayload));
                                }
                            } catch (IOException e) {
                                LOGGER.error("Error parsing JSON payload.", e);
                            } catch (SpelEvaluationException e) {
                                LOGGER.error(MessageFormat
                                        .format("Error evaluating, probably malformed, expression: \"{0}\". Message payload: {1} ",
                                                expression.get(),
                                                incomingPayload), e);
                            }
                        } else {
                            forwardEvent(eventRule.getOutgoing(),event);
                            outEvents.add(event);
                        }

                        break;
                    }
                }
            }
        }
        return new AsyncResult<List<Event>>(outEvents);
    }

    private void forwardEvent(EventRule.RuleActor outgoing, Event event) {
        EventRulePublisher eventRulePublisher = (EventRulePublisher) applicationContext.getBean(outgoing.getUri().getScheme());
        eventRulePublisher.send(event, outgoing);
    }
}
