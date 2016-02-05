package com.konkerlabs.platform.registry.business.services.rules;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleExecutor;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRulePublisher;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventRuleExecutorImpl implements EventRuleExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventRuleExecutorImpl.class);

    private EventRuleService eventRuleService;

    private EventRulePublisher eventRulePublisher;

    private enum RuleTransformationType {CONTENT_MATCH}

    ;

    //FIXME Apply strategy dynamically, according to the outgoing RuleActor defined in the EventRule
    @Autowired
    public EventRuleExecutorImpl(EventRuleService eventRuleService, @Qualifier("eventRulePublisherMqtt") EventRulePublisher eventRulePublisher) {
        this.eventRuleService = eventRuleService;
        this.eventRulePublisher = eventRulePublisher;
    }

    @Async
    @Override
    public void execute(Event event, URI uri) {
        List<EventRule> eventRules = eventRuleService.findByIncomingUri(uri);
        String incomingPayload = event.getPayload();

        //FIXME execute event rules in parallel
        for (EventRule eventRule : eventRules) {
            if (!eventRule.isActive())
                continue;

            for (EventRule.RuleTransformation ruleTransformation : eventRule.getTransformations()) {
                String transformationValue = ruleTransformation.getData().get("value");
                switch (RuleTransformationType.valueOf(ruleTransformation.getType())) {
                    case CONTENT_MATCH:
                        if (incomingPayload.contains(transformationValue)) {
                            eventRulePublisher.send(Event.builder().timestamp(Instant.now()).payload(incomingPayload).build(), eventRule.getOutgoing());
                        } else {
                            LOGGER.debug(MessageFormat.format("Dropped rule \"{0}\", not matching \"{1}\" pattern with content \"{2}\". Message payload: {3} ",
                                    eventRule.getName(), ruleTransformation.getType(), transformationValue, incomingPayload));
                        }
                        break;
                }
            }
        }
    }


}
