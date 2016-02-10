package com.konkerlabs.platform.registry.business.services.rules.publishers;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRulePublisher;
import com.konkerlabs.platform.registry.integration.gateways.SMSMessageGateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("eventRulePublisherTwilio")
@Qualifier("eventRulePublisherTwilio")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventRulePublisherTwilio implements EventRulePublisher {
    
    @Autowired
    private SMSMessageGateway messageGateway;
    
    @Override
    public void send(Event outgoingEvent, EventRule.RuleActor outgoingRuleActor) {
        // TODO: call the message gateway
    }
}
