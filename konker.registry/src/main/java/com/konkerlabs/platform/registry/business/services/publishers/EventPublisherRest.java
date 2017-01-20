package com.konkerlabs.platform.registry.business.services.publishers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.utilities.expressions.ExpressionEvaluationService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
    private JsonParsingService jsonParsingService;
    private ExpressionEvaluationService expressionEvaluationService;
    private EventRepository eventRepository;

    @Autowired
    public EventPublisherRest(HttpGateway httpGateway,
                              RestDestinationService restDestinationService,
                              JsonParsingService jsonParsingService,
                              ExpressionEvaluationService expressionEvaluationService) {
        this.httpGateway = httpGateway;
        this.restDestinationService = restDestinationService;
        this.jsonParsingService = jsonParsingService;
        this.expressionEvaluationService = expressionEvaluationService;
    }

    @Autowired @Qualifier("mongoEvents")
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

                String serviceURI = evaluateExpressionIfNecessary(
                    destination.getResult().getServiceURI(),outgoingEvent.getPayload()
                );

                httpGateway.request(
                    HttpMethod.resolve(
                            Optional.ofNullable(destination.getResult().getMethod()).isPresent() ?
                                    destination.getResult().getMethod() : "POST"),
                    URI.create(serviceURI), MediaType.APPLICATION_JSON,
                    () -> outgoingEvent.getPayload(),
                    destination.getResult().getServiceUsername(),
                    destination.getResult().getServicePassword()
                );
//                eventRepository.saveIncoming(tenant,outgoingEvent);
            } catch (JsonProcessingException|IntegrationException e) {
                LOGGER.error("Failed to forward event to its destination",
                        tenant.toURI(),
                        tenant.getLogLevel(),
                        e);
            }
        } else {
            LOGGER.debug(
                    MessageFormat.format(EVENT_DROPPED,destinationUri,outgoingEvent.getPayload()), 
                    tenant.toURI(),
                    tenant.getLogLevel());
        }
    }

    private String evaluateExpressionIfNecessary(String template, String json) throws JsonProcessingException {
        if (ExpressionEvaluationService.EXPRESSION_TEMPLATE_PATTERN.matcher(template).matches())
            return expressionEvaluationService.evaluateTemplate(template,jsonParsingService.toMap(json));
        else return template;
    }
}
