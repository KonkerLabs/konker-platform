package com.konkerlabs.platform.registry.data.services.publishers;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.utilities.expressions.ExpressionEvaluationService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;

@Service(RESTDestinationURIDealer.REST_DESTINATION_URI_SCHEME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventPublisherRest implements EventPublisher {

    private static final String EVENT_DROPPED = "Outgoing event has been dropped: [URI: {0}] - [Message: {1}]";
    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisherRest.class);

    private HttpGateway httpGateway;
    private RestDestinationService restDestinationService;
    private JsonParsingService jsonParsingService;
    private ExpressionEvaluationService expressionEvaluationService;

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

    @Override
    public void send(Event outgoingEvent, URI destinationUri, Map<String, String> data, Tenant tenant, Application application) {
        Optional.ofNullable(outgoingEvent)
                .orElseThrow(() -> new IllegalArgumentException("Event cannot be null"));
        Optional.ofNullable(destinationUri)
                .filter(uri -> !uri.toString().isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Destination URI cannot be null or empty"));
        Optional.ofNullable(tenant)
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));
        Optional.ofNullable(application)
                .orElseThrow(() -> new IllegalArgumentException("Application cannot be null"));

        ServiceResponse<RestDestination> destination = restDestinationService.getByGUID(
                tenant,
                application,
                destinationUri.getPath().replaceAll("/", "")
        );

        Optional.ofNullable(destination)
                .filter(response -> response.getStatus().equals(ServiceResponse.Status.OK))
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageFormat.format("REST Destination is unknown : {0}", destinationUri)
                ));

        RestDestination restDestination = destination.getResult();
		if (restDestination.isActive()) {
            try {

                String serviceURI = evaluateExpressionIfNecessary(
                        restDestination.getServiceURI(), outgoingEvent.getPayload()
                );

                httpGateway.request(
                        HttpMethod.resolve(
                                Optional.ofNullable(restDestination.getMethod()).isPresent() ?
                                        restDestination.getMethod() : "POST"),
                        getHeaders(restDestination),
                        URI.create(UriUtils.encodeQuery(serviceURI, "UTF-8")), MediaType.APPLICATION_JSON,
                        () -> "FORWARD_MESSAGE".equals(restDestination.getType().name()) ? outgoingEvent.getPayload() : restDestination.getBody(),
                        restDestination.getServiceUsername(),
                        restDestination.getServicePassword()
                );
//                eventRepository.saveIncoming(tenant,outgoingEvent);
            } catch (IllegalArgumentException | JsonProcessingException | IntegrationException | UnsupportedEncodingException e) {
                LOGGER.error("Failed to forward event to its destination",
                        tenant.toURI(),
                        tenant.getLogLevel(),
                        e);
            }
        } else {
            LOGGER.debug(
                    MessageFormat.format(EVENT_DROPPED, destinationUri, outgoingEvent.getPayload()),
                    tenant.toURI(),
                    tenant.getLogLevel());
        }
    }

	private HttpHeaders getHeaders(RestDestination restDestination) {
		HttpHeaders headers = new HttpHeaders();
		if (restDestination.getHeaders() != null) {
			for (String key : restDestination.getHeaders().keySet()) {
				headers.add(key, restDestination.getHeaders().get(key));
			}
		}
		return headers;
	}

    private String evaluateExpressionIfNecessary(String template, String json) throws JsonProcessingException {
        if (ExpressionEvaluationService.EXPRESSION_TEMPLATE_PATTERN.matcher(template).matches())
            return expressionEvaluationService.evaluateTemplate(template, jsonParsingService.toMap(json));
        else return template;
    }
}
