package com.konkerlabs.platform.registry.business.services.publishers;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsURIDealer;
import com.konkerlabs.platform.registry.business.repositories.solr.EventRepository;
import com.konkerlabs.platform.registry.business.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.SMSMessageGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Service(SmsURIDealer.SMS_URI_SCHEME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventPublisherSms implements EventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisherSms.class);

    private SMSMessageGateway messageGateway;
    private EventRepository eventRepository;

    @Autowired
    public EventPublisherSms(SMSMessageGateway messageGateway) {
        this.messageGateway = messageGateway;
    }

    @Autowired
    public void setEventRepository(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void send(Event outgoingEvent, URI destinationUri, Map<String, String> data, Tenant tenant) {
        Optional.ofNullable(outgoingEvent)
                .orElseThrow(() -> new IllegalArgumentException("Event cannot be null"));
        Optional.ofNullable(destinationUri)
                .filter(uri -> !uri.toString().isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Destination URI cannot be null or empty"));
        Optional.ofNullable(tenant)
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));

        try {
            messageGateway.send("You have received a message from Konker device: " + outgoingEvent.getPayload(),
                    destinationUri.getAuthority());
            eventRepository.push(tenant,outgoingEvent);
        } catch (IntegrationException|BusinessException e) {
            LOGGER.error("Error sending SMS.", e);
        }
    }
}
