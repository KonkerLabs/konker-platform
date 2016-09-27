package com.konkerlabs.platform.registry.business.services.routes;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.routes.api.EventTransformationService;
import com.konkerlabs.platform.utilities.expressions.ExpressionEvaluationService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

import static com.konkerlabs.platform.registry.business.services.publishers.EventPublisherDevice.DEVICE_MQTT_CHANNEL;

@Service
public class EventRouteExecutorImpl implements EventRouteExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventRouteExecutorImpl.class);

    @Autowired
    private EventRouteService eventRouteService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ExpressionEvaluationService evaluationService;
    @Autowired
    private JsonParsingService jsonParsingService;
    @Autowired
    private EventTransformationService eventTransformationService;

    @Override
    public Future<List<Event>> execute(Event event, URI uri) {

        NewServiceResponse<List<EventRoute>> serviceResponse = eventRouteService.findByIncomingUri(uri);

        List<Event> outEvents = new ArrayList<Event>();

        if (serviceResponse.isOk()) {
            List<EventRoute> eventRoutes = serviceResponse.getResult();

            String incomingPayload = event.getPayload();

            for (EventRoute eventRoute : eventRoutes) {
                if (!eventRoute.isActive())
                    continue;
                if (!eventRoute.getIncoming().getData().get(DEVICE_MQTT_CHANNEL).equals(event.getChannel())) {
                    LOGGER.debug("Non matching channel for incoming event: {}", event);
                    continue;
                }

                try {
                    if (isFilterMatch(event, eventRoute)) {
                        if (Optional.ofNullable(eventRoute.getTransformation()).isPresent()) {
                            Optional<Event> transformed = eventTransformationService.transform(
                                    event, eventRoute.getTransformation());
                            if (transformed.isPresent()) {
                                forwardEvent(eventRoute, transformed.get());
                                outEvents.add(transformed.get());
                            } else {
                                logEventWithInvalidTransformation(event, eventRoute);
                            }
                        } else {
                            forwardEvent(eventRoute, event);
                            outEvents.add(event);
                        }
                    } else {
                        logEventFilterMismatch(event, eventRoute);
                    }
                } catch (IOException e) {
                    LOGGER.error("Error parsing JSON payload.", e);
                } catch (SpelEvaluationException e) {
                    LOGGER.error(MessageFormat
                            .format("Error evaluating, probably malformed, expression: \"{0}\". Message payload: {1} ",
                                    eventRoute.getFilteringExpression(),
                                    incomingPayload), e);
                }
            }
        }

        return new AsyncResult<List<Event>>(outEvents);
    }

    private boolean isFilterMatch(Event event, EventRoute eventRoute) throws JsonProcessingException {
        Optional<String> expression = Optional.ofNullable(eventRoute.getFilteringExpression())
                .filter(filter -> !filter.isEmpty());

        if (expression.isPresent()) {
            Map<String, Object> objectMap = jsonParsingService.toMap(event.getPayload());
            return evaluationService.evaluateConditional(expression.get(), objectMap);
        } else
            return true;
    }

    private void forwardEvent(EventRoute eventRoute, Event event) {
        EventPublisher eventPublisher = (EventPublisher) applicationContext
                .getBean(eventRoute.getOutgoing().getUri().getScheme());
        eventPublisher.send(event, eventRoute.getOutgoing().getUri(),
                eventRoute.getOutgoing().getData(),
                eventRoute.getTenant());
    }

    private void logEventFilterMismatch(Event event, EventRoute eventRoute) {
        LOGGER.debug(MessageFormat.format("Dropped route \"{0}\", not matching pattern with content \"{1}\". Message payload: {2} ",
                eventRoute.getName(), eventRoute.getFilteringExpression(), event.getPayload()));
    }

    private void logEventWithInvalidTransformation(Event event, EventRoute eventRoute) {
        LOGGER.debug(MessageFormat.format("Dropped route \"{0}\" with invalid transformation. Message payload: {1} ",
                eventRoute.getName(), event.getPayload()));
    }
}
