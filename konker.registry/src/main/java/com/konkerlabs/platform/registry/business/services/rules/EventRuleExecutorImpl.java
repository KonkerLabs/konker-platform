package com.konkerlabs.platform.registry.business.services.rules;

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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    private enum RuleTransformationType {CONTENT_MATCH}

    @Async
    @Override
    public Future<List<Event>> execute(Event event, URI uri) {
        List<EventRule> eventRules = eventRuleService.findByIncomingUri(uri);
        String incomingPayload = event.getPayload();

        List<Event> outEvents = new ArrayList<Event>();

        //FIXME execute event rules in parallel
        for (EventRule eventRule : eventRules) {
            if (!eventRule.isActive() || !eventRule.getIncoming().getData().get("channel").equals(event.getChannel()))
                continue;

            for (EventRule.RuleTransformation ruleTransformation : eventRule.getTransformations()) {
                String transformationValue = ruleTransformation.getData().get("value");
                switch (RuleTransformationType.valueOf(ruleTransformation.getType())) {
                    case CONTENT_MATCH:
                        if (incomingPayload.contains(transformationValue)) {
                            eventRulePublisher = (EventRulePublisher) applicationContext.getBean(eventRule.getOutgoing().getUri().getScheme());
                            Event outEvent = Event.builder().timestamp(Instant.now()).payload(incomingPayload).build();
                            eventRulePublisher.send(outEvent, eventRule.getOutgoing());
                            outEvents.add(outEvent);
                        } else {
                            LOGGER.debug(MessageFormat.format("Dropped rule \"{0}\", not matching \"{1}\" pattern with content \"{2}\". Message payload: {3} ",
                                    eventRule.getName(), ruleTransformation.getType(), transformationValue, incomingPayload));
                        }
                        break;
                }
            }
        }
        return new AsyncResult<List<Event>>(outEvents);
    }


}
