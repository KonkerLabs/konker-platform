package com.konkerlabs.platform.registry.business.services.rules.publishers;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRulePublisher;
import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.SMSMessageGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("sms")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventRulePublisherTwilio implements EventRulePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventRulePublisherTwilio.class);

    @Autowired
    private SMSMessageGateway messageGateway;
    
    @Override
    public void send(Event outgoingEvent, EventRule.RuleActor outgoingRuleActor) {
        try {
            messageGateway.send("You have received a message from Konker device: " + outgoingEvent.getPayload(),
                    outgoingRuleActor.getUri().getAuthority());
        } catch (IntegrationException e) {
            LOGGER.error("Error sending SMS.", e);
        }
    }
}
