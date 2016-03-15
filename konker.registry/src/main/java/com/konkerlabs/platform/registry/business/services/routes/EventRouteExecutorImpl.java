package com.konkerlabs.platform.registry.business.services.routes;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRoutePublisher;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteService;
import com.konkerlabs.platform.utilities.expressions.ExpressionEvaluationService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.scheduling.annotation.Async;
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

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
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

    public enum RuleTransformationType {EXPRESSION_LANGUAGE}

    @Async
    @Override
    public Future<List<Event>> execute(Event event, URI uri) {

        ServiceResponse<List<EventRoute>> serviceResponse = eventRouteService.findByIncomingUri(uri);

        List<Event> outEvents = new ArrayList<Event>();

        switch (serviceResponse.getStatus()) {
            case OK:
                List<EventRoute> eventRoutes = serviceResponse.getResult();

                String incomingPayload = event.getPayload();

                //FIXME execute event routes in parallel
                for (EventRoute eventRoute : eventRoutes) {
                    if (!eventRoute.isActive())
                        continue;
                    if (!eventRoute.getIncoming().getData().get("channel").equals(event.getChannel())) {
                        LOGGER.debug("Non matching channel for incoming event: {}", event);
                        continue;
                    }

                    try {
                        if (isFilterMatch(event, eventRoute)) {
                            forwardEvent(eventRoute.getOutgoing(), event);
                            outEvents.add(event);
                        } else {
                            LOGGER.debug(MessageFormat.format("Dropped route \"{0}\", not matching pattern with content \"{1}\". Message payload: {2} ",
                                    eventRoute.getName(), eventRoute.getFilteringExpression(), incomingPayload));
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
            default:
                LOGGER.debug(MessageFormat.format("No routes found for Incoming URI: {0}", uri));
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

    private void forwardEvent(EventRoute.RouteActor outgoing, Event event) {
        EventRoutePublisher eventRoutePublisher = (EventRoutePublisher) applicationContext.getBean(outgoing.getUri().getScheme());
        eventRoutePublisher.send(event, outgoing);
    }
}
