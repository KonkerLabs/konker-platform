package com.konkerlabs.platform.registry.data.services.publishers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.SmsDestinationService;
import com.konkerlabs.platform.registry.data.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.SMSMessageGateway;
import com.konkerlabs.platform.utilities.expressions.ExpressionEvaluationService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;

@Service(SmsDestinationURIDealer.SMS_URI_SCHEME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventPublisherSms implements EventPublisher {

    private static final String EVENT_DROPPED = "Outgoing event has been dropped: [URI: {0}] - [Message: {1}]";
    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisherSms.class);

    private SMSMessageGateway messageGateway;
    private SmsDestinationService smsDestinationService;
    private JsonParsingService jsonParsingService;
    private ExpressionEvaluationService expressionEvaluationService;

    @Autowired
    public EventPublisherSms(SMSMessageGateway messageGateway,
                             SmsDestinationService smsDestinationService,
                             JsonParsingService jsonParsingService,
                             ExpressionEvaluationService expressionEvaluationService) {
        this.messageGateway = messageGateway;
        this.smsDestinationService = smsDestinationService;
        this.jsonParsingService = jsonParsingService;
        this.expressionEvaluationService = expressionEvaluationService;
    }

    @Override
    public void send(Event outgoingEvent, URI destinationUri, Map<String, String> data, Tenant tenant) {
        Optional.ofNullable(outgoingEvent)
                .orElseThrow(() -> new IllegalArgumentException("Event cannot be null"));
        Optional.ofNullable(destinationUri)
                .filter(uri -> !uri.toString().isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Destination URI cannot be null or empty"));
        Optional.ofNullable(data)
                .orElseThrow(() -> new IllegalArgumentException("Data cannot be null"));
        Optional.ofNullable(data.get(EventRoute.SMS_MESSAGE_STRATEGY_PARAMETER_NAME))
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalStateException("A SMS message strategy is required"));
        Optional.ofNullable(data.get(EventRoute.SMS_MESSAGE_STRATEGY_PARAMETER_NAME))
                .filter(s -> s.equals(EventRoute.SMS_MESSAGE_CUSTOM_STRATEGY_PARAMETER_VALUE))
                .ifPresent(s1 -> {
                    Optional.ofNullable(data.get(EventRoute.SMS_MESSAGE_TEMPLATE_PARAMETER_NAME))
                        .filter(s -> !s.isEmpty())
                        .orElseThrow(() -> new IllegalStateException("A message template is required on custom strategy"));
                });
        Optional.ofNullable(tenant)
                .orElseThrow(() -> new IllegalArgumentException("Tenant cannot be null"));

        ServiceResponse<SmsDestination> destination = smsDestinationService.getByGUID(
                tenant,
                destinationUri.getPath().replaceAll("/","")
        );

        Optional.ofNullable(destination)
                .filter(response -> response.getStatus().equals(ServiceResponse.Status.OK))
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageFormat.format("SMS Destination is unknown : {0}",destinationUri)
                ));

        if (destination.getResult().isActive()) {
            try {

                String messageBody = null;

                switch (data.get(EventRoute.SMS_MESSAGE_STRATEGY_PARAMETER_NAME)) {
                    case EventRoute.SMS_MESSAGE_CUSTOM_STRATEGY_PARAMETER_VALUE: {
                        messageBody = evaluateExpressionIfNecessary(
                                data.get(EventRoute.SMS_MESSAGE_TEMPLATE_PARAMETER_NAME),outgoingEvent.getPayload()
                        );
                        break;
                    }
                    default: messageBody = outgoingEvent.getPayload();
                }

                messageGateway.send(messageBody,destination.getResult().getPhoneNumber());
//                eventRepository.saveIncoming(tenant, outgoingEvent);
            } catch (JsonProcessingException|IntegrationException e) {
                LOGGER.error("Error sending SMS.",
                        tenant.toURI(),
                        tenant.getLogLevel(),
                        e);
            }
        } else {
            LOGGER.debug(
                    MessageFormat.format(EVENT_DROPPED,destinationUri,outgoingEvent.getPayload()),
                    tenant.toURI(),
                    tenant.getLogLevel()
            );
        }
    }

    private String evaluateExpressionIfNecessary(String template, String json) throws JsonProcessingException {
        if (ExpressionEvaluationService.EXPRESSION_TEMPLATE_PATTERN.matcher(template).matches())
            return expressionEvaluationService.evaluateTemplate(template,jsonParsingService.toMap(json));
        else return template;
    }
}
