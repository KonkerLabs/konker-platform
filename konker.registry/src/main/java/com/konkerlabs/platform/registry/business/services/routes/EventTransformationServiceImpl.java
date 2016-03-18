package com.konkerlabs.platform.registry.business.services.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.TransformationStep;
import com.konkerlabs.platform.registry.business.services.routes.api.EventTransformationService;
import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.utilities.expressions.ExpressionEvaluationService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Component
public class EventTransformationServiceImpl implements EventTransformationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventTransformationService.class);

    @Autowired
    private JsonParsingService jsonParsingService;
    @Autowired
    private ExpressionEvaluationService evaluationService;
    @Autowired
    private HttpGateway httpGateway;

    @Override
    public Optional<Event> transform(Event original, Transformation transformation) {
        Optional.ofNullable(original)
            .orElseThrow(() -> new IllegalArgumentException("Event cannot be null"));
        Optional.ofNullable(transformation)
                .orElseThrow(() -> new IllegalArgumentException("Transformation cannot be null"));

        Optional<Event> transformed = Optional.of(
            Event.builder()
                .channel(original.getChannel())
                .payload(original.getPayload())
                .timestamp(original.getTimestamp())
                .build()
        );

        for (TransformationStep step : transformation.getSteps()) {
            if (transformed.isPresent())
                transformed = Optional.ofNullable(applyStep(transformed.get(),step));
            else break;
        }

        return transformed;
    }

    private Event applyStep(Event event, TransformationStep step) {
        try {
            String stepUrl = evaluationService
                    .evaluateTemplate(
                            step.getAttributes().get(RestTransformationStep.REST_URL_ATTRIBUTE_NAME),
                            jsonParsingService.toMap(event.getPayload()));

            String stepResponse = httpGateway.request(HttpMethod.POST,
                    new URI(stepUrl),
                    () -> event.getPayload(),
                    step.getAttributes().get(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME),
                    step.getAttributes().get(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME));

            if (isValidResponse(stepResponse))
                return Event.builder()
                        .channel(event.getChannel())
                        .timestamp(event.getTimestamp())
                        .payload(stepResponse).build();
            else
                return null;
        } catch (JsonProcessingException e) {
            LOGGER.error("Malformed JSON",e);
            return null;
        } catch (ParseException|EvaluationException e) {
            LOGGER.error("Malformed URL template",e);
            return null;
        } catch (IntegrationException e) {
            LOGGER.error("Failed to request step URL",e);
            return null;
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to encode step URL",e);
            return null;
        }
    }

    private boolean isValidResponse(String stepResponse) {
        return Optional.ofNullable(stepResponse)
            .filter(s -> !s.isEmpty() && !s.trim().equals("[]") && !s.trim().equals("{}"))
            .isPresent();
    }
}
