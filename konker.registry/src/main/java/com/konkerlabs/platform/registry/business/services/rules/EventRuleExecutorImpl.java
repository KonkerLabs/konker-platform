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
import java.time.Instant;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventRuleExecutorImpl implements EventRuleExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventRuleExecutorImpl.class);

    private EventRuleService eventRuleService;

    private EventRulePublisher eventRulePublisher;

    private enum RuleTransformationType {CONTENT_MATCH};

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
                switch (RuleTransformationType.valueOf(ruleTransformation.getType())) {
                    case CONTENT_MATCH:
                        if (incomingPayload.contains(ruleTransformation.getData().get("value"))) {
                            eventRulePublisher.send(Event.builder().timestamp(Instant.now()).payload(incomingPayload).build(), eventRule.getOutgoing());
                        }
                        break;
                }
            }
        }
    }


}
