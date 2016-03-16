package com.konkerlabs.platform.registry.business.services.publishers;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;
import com.konkerlabs.platform.registry.business.repositories.solr.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;

@Service(RESTDestinationURIDealer.REST_DESTINATION_URI_SCHEME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventPublisherRest implements EventPublisher {

    private static final String EVENT_DROPPED = "Outgoing event has been dropped: [URI: {0}] - [Message: {1}]";

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisherRest.class);

    private HttpGateway httpGateway;
    private RestDestinationService restDestinationService;
    private EventRepository eventRepository;

    @Autowired
    public EventPublisherRest(HttpGateway httpGateway, RestDestinationService restDestinationService) {
        this.httpGateway = httpGateway;
        this.restDestinationService = restDestinationService;
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

        ServiceResponse<RestDestination> destination = restDestinationService.getByGUID(
            tenant,
            destinationUri.getPath().replaceAll("/","")
        );

        Optional.ofNullable(destination)
                .filter(response -> response.getStatus().equals(ServiceResponse.Status.OK))
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageFormat.format("REST Destination is unknown : {0}",destinationUri)
                ));

        if (destination.getResult().isActive()) {
            try {
                httpGateway.request(
                    HttpMethod.POST,
                    URI.create(destination.getResult().getServiceURI()),
                    () -> outgoingEvent.getPayload(),
                    destination.getResult().getServiceUsername(),
                    destination.getResult().getServicePassword(),
                    HttpStatus.OK
                );
                eventRepository.push(tenant,outgoingEvent);
            } catch (BusinessException|IntegrationException e) {
                LOGGER.error("Failed to forward event to its destination", e);
            }
        } else {
            LOGGER.debug(
                    MessageFormat.format(EVENT_DROPPED,destinationUri,outgoingEvent.getPayload())
            );
        }
    }
}
