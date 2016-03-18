package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.konkerlabs.platform.registry.business.services.publishers.EventPublisherSms;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Document(collection = "eventRoutes")
@Data
@Builder
public class EventRoute {

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    private String name;
    private String description;
    private RouteActor incoming;
    private RouteActor outgoing;
    private String filteringExpression;
    @DBRef
    private Transformation transformation;
    private boolean active;

    public List<String> applyValidations() {
        List<String> validations = new ArrayList<>();

        if (getTenant() == null)
            validations.add("Tenant cannot be null");
        if (getName() == null || getName().isEmpty())
            validations.add("Name cannot be null or empty");
        if (getIncoming() == null)
            validations.add("Incoming actor cannot be null");
        if (getIncoming() != null && getIncoming().getUri() == null)
            validations.add("Incoming actor URI cannot be null");
        if (getIncoming() != null && getIncoming().getUri() != null && getIncoming().getUri().toString().isEmpty())
            validations.add("Incoming actor's URI cannot be empty");
        if (getOutgoing() == null)
            validations.add("Outgoing actor cannot be null");
        if (getOutgoing() != null && getOutgoing().getUri() == null)
            validations.add("Outgoing actor URI cannot be null");
        if (getOutgoing() != null && getOutgoing().getUri() != null && getOutgoing().getUri().toString().isEmpty())
            validations.add("Outgoing actor's URI cannot be empty");
        
        if ("sms".equals(Optional.of(getOutgoing()).map(RouteActor::getUri).map(URI::getScheme).orElse(""))) {
            applySMSOutgoingValidations(validations);
        }

        if (validations.isEmpty())
            return null;
        else
            return validations;
    }

    public List<String> applySMSOutgoingValidations(List<String> validations) {
        Map<String, String> data = getOutgoing().getData();
        if (!"sms".equals(getOutgoing().getUri().getScheme())) {
            validations.add("SMS Outgoing URI must be an SMS URI");
        } else {
            if (EventPublisherSms.SMS_MESSAGE_CUSTOM_STRATEGY_PARAMETER_VALUE.equals(data.get(EventPublisherSms.SMS_MESSAGE_STRATEGY_PARAMETER_NAME))) {
                if (!Optional.ofNullable(data.get(EventPublisherSms.SMS_MESSAGE_TEMPLATE_PARAMETER_NAME)).filter(t -> !t.trim().isEmpty()).isPresent())
                    validations.add("Custom Text is Mandatory if SMS Strategy is Custom Text");
            } else if (!EventPublisherSms.SMS_MESSAGE_FORWARD_STRATEGY_PARAMETER_VALUE.equals(data.get(EventPublisherSms.SMS_MESSAGE_STRATEGY_PARAMETER_NAME))) {
                validations.add(MessageFormat.format("Invalid SMS Strategy: {0}", data.get(EventPublisherSms.SMS_MESSAGE_STRATEGY_PARAMETER_NAME)));
            }
        }
        
        return validations;
    }

    @Data
    @Builder
    public static class RouteActor {
        private URI uri;
        private Map<String, String> data = new HashMap<>();
    }
}
