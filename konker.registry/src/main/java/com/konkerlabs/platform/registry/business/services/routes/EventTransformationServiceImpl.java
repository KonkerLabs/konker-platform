package com.konkerlabs.platform.registry.business.services.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.TransformationStep;
import com.konkerlabs.platform.registry.business.model.enumerations.SupportedHttpMethod;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.regex.Matcher;

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
                .incoming(original.getIncoming())
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
                            (String) step.getAttributes().get(RestTransformationStep.REST_URL_ATTRIBUTE_NAME),
                            jsonParsingService.toMap(event.getPayload()));

            String stepMethod = evaluationService
                    .evaluateTemplate(
                            (String) step.getAttributes().get(RestTransformationStep.REST_ATTRIBUTE_METHOD),
                            jsonParsingService.toMap(event.getPayload()));

            if(StringUtils.isEmpty(stepMethod)){
                stepMethod = SupportedHttpMethod.POST.getCode();
            }

            HttpHeaders headers = new HttpHeaders();
            Optional.ofNullable((String) step.getAttributes().get(RestTransformationStep.REST_ATTRIBUTE_HEADERS))
                    .ifPresent( item ->{
                        String[] header = item.split("=");
                        if(header.length == 2){
                            headers.add(header[0], header[1]);
                        }
                    });

            String stepResponse = httpGateway.request(
                    HttpMethod.resolve(stepMethod),
                    headers,
                    new URI(stepUrl), MediaType.APPLICATION_JSON,
                    () -> event.getPayload(),
                    (String) step.getAttributes().get(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME),
                    (String) step.getAttributes().get(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME));

            if (isValidResponse(stepResponse))
                return Event.builder()
                        .incoming(event.getIncoming())
                        .timestamp(event.getTimestamp())
                        .payload(stepResponse).build();
            else
                return null;
        } catch (JsonProcessingException e) {
            LOGGER.error("Malformed JSON", e);
            return null;
        } catch (ParseException|EvaluationException e) {
            LOGGER.error("Malformed URL template", e);
            return null;
        } catch (IntegrationException e) {
            LOGGER.error("Failed to request step URL", e);
            return null;
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to encode step URL", e);
            return null;
        }
    }

    private boolean isValidResponse(String stepResponse) {
        return Optional.ofNullable(stepResponse)
            .filter(s -> !s.isEmpty() && !s.trim().equals("[]") && !s.trim().equals("{}"))
            .isPresent();
    }
}
