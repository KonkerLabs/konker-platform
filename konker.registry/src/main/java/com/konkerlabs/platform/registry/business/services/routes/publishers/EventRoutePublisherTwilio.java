package com.konkerlabs.platform.registry.business.services.routes.publishers;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRoutePublisher;
import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.SMSMessageGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("sms")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventRoutePublisherTwilio implements EventRoutePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventRoutePublisherTwilio.class);

    @Autowired
    private SMSMessageGateway messageGateway;
    
    @Override
    public void send(Event outgoingEvent, EventRoute.RuleActor outgoingRuleActor) {
        try {
            messageGateway.send("You have received a message from Konker device: " + outgoingEvent.getPayload(),
                    outgoingRuleActor.getUri().getAuthority());
        } catch (IntegrationException e) {
            LOGGER.error("Error sending SMS.", e);
        }
    }
}
