package com.konkerlabs.platform.registry.business.services.routes;


import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRoute;
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
        List<EventRoute> eventRoutes = eventRouteService.findByIncomingUri(uri);
        String incomingPayload = event.getPayload();

        List<Event> outEvents = new ArrayList<Event>();

        //FIXME execute event routes in parallel
        for (EventRoute eventRoute : eventRoutes) {
            if (!eventRoute.isActive())
                continue;
            if (!eventRoute.getIncoming().getData().get("channel").equals(event.getChannel())) {
                LOGGER.debug("Non matching channel for incoming event: {}", event);
                continue;
            }

            for (EventRoute.RuleTransformation ruleTransformation : eventRoute.getTransformations()) {
                switch (RuleTransformationType.valueOf(ruleTransformation.getType())) {
                    case EXPRESSION_LANGUAGE: {

                        Optional<String> expression = Optional.ofNullable(ruleTransformation.getData().get("value"))
                                .filter(filter -> !filter.isEmpty());

                        if (expression.isPresent()) {
                            try {
                                Map<String, Object> objectMap = jsonParsingService.toMap(incomingPayload);

                                if (evaluationService.evaluateConditional(expression.get(), objectMap)) {
                                    forwardEvent(eventRoute.getOutgoing(), event);
                                    outEvents.add(event);
                                } else {
                                    LOGGER.debug(MessageFormat.format("Dropped route \"{0}\", not matching \"{1}\" pattern with content \"{2}\". Message payload: {3} ",
                                            eventRoute.getName(), ruleTransformation.getType(), expression.get(), incomingPayload));
                                }
                            } catch (IOException e) {
                                LOGGER.error("Error parsing JSON payload.", e);
                            } catch (SpelEvaluationException e) {
                                LOGGER.error(MessageFormat
                                        .format("Error evaluating, probably malformed, expression: \"{0}\". Message payload: {1} ",
                                                expression.get(),
                                                incomingPayload), e);
                            }
                        } else {
                            forwardEvent(eventRoute.getOutgoing(),event);
                            outEvents.add(event);
                        }

                        break;
                    }
                }
            }
        }
        return new AsyncResult<List<Event>>(outEvents);
    }

    private void forwardEvent(EventRoute.RuleActor outgoing, Event event) {
        EventRoutePublisher eventRoutePublisher = (EventRoutePublisher) applicationContext.getBean(outgoing.getUri().getScheme());
        eventRoutePublisher.send(event, outgoing);
    }
}
