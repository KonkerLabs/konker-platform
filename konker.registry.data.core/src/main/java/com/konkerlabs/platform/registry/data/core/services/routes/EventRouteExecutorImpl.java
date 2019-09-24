package com.konkerlabs.platform.registry.data.core.services.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.repositories.DeviceModelRepository;
import com.konkerlabs.platform.registry.business.services.LocationTreeUtils;
import com.konkerlabs.platform.registry.business.services.api.EventRouteCounterService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.LocationSearchService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.core.integration.gateway.RabbitGateway;
import com.konkerlabs.platform.registry.data.core.services.publishers.api.EventPublisher;
import com.konkerlabs.platform.registry.data.core.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.registry.data.core.services.routes.api.EventTransformationService;
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
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Future;

import static com.konkerlabs.platform.registry.data.core.services.publishers.EventPublisherDevice.DEVICE_MQTT_CHANNEL;

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
    @Autowired
    private LocationSearchService locationSearchService;
    @Autowired
    private DeviceModelRepository deviceModelRepository;
    @Autowired
    private RabbitGateway rabbitGateway;
    @Autowired
    private EventRouteCounterService eventRouteCounterService;

    @Override
    public Future<List<Event>> execute(Event event, Device device) {

        List<Event> outEvents = new ArrayList<>();

        ServiceResponse<List<EventRoute>> serviceRoutes = eventRouteService.getAll(device.getTenant(), device.getApplication());
        if (!serviceRoutes.isOk()) {
            LOGGER.error("Error listing application events routes", device.toURI(), device.getTenant().getLogLevel());
            return new AsyncResult<>(outEvents);
        }

        if (event.getIncoming().getChannel().equals(EventRouteExecutor.ECHO_CHANNEL)) {
            rabbitGateway.queueEvent(device, EventRoute.builder().build(), event);
        }

        List<EventRoute> eventRoutes = serviceRoutes.getResult();
        if (eventRoutes.isEmpty()) {
            return new AsyncResult<>(outEvents);
        }

        eventRoutes.parallelStream().forEach((eventRoute) -> {
            if (isEventRouteDeviceMatch(eventRoute, device, event)) {
                rabbitGateway.queueEvent(device, eventRoute, event);
            }
        });

        return new AsyncResult<>(outEvents);
    }

    @Override
    public void execute(Event event, Device device, EventRoute eventRoute) throws Exception {
        List<Event> outEvents = new ArrayList<>();
        processEventRoute(event, outEvents, eventRoute);

    }

    private void processEventRoute(Event event, List<Event> outEvents, EventRoute eventRoute) throws Exception {

        String incomingPayload = event.getPayload();

        if (!eventRoute.isActive())
            return;

        if (!eventRoute.getIncoming().isApplication()
                && !eventRoute.getIncoming().getData().get(DEVICE_MQTT_CHANNEL).equals(event.getIncoming().getChannel())
                && !EventRouteExecutor.ECHO_CHANNEL.equals(event.getIncoming().getChannel())) {
            LOGGER.debug("Non matching channel for incoming event: {}", event, eventRoute.getTenant().toURI(), eventRoute.getTenant().getLogLevel());
            return;
        }

        try {
            if (isFilterExpressionMatch(event, eventRoute)) {
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
            LOGGER.error("Error parsing JSON payload.", eventRoute.toURI(), eventRoute.getTenant().getLogLevel(), e);
        } catch (SpelEvaluationException e) {
            LOGGER.error(MessageFormat
                    .format("Error evaluating, probably malformed, expression: \"{0}\". Message payload: {1} ",
                            eventRoute.getFilteringExpression(),
                            incomingPayload), eventRoute.toURI(), eventRoute.getTenant().getLogLevel(), e);
        }

    }

    private boolean isEventRouteDeviceMatch(EventRoute eventRoute, Device device, Event event) {

        if (event.getIncoming().getChannel().equals(DEBUG_CHANNEL)) {
            return false;
        }

        // match device actor
        if (eventRoute.getIncoming().getUri().equals(device.toURI())) {
            return true;
        }

        // match model location actor
        if (eventRoute.getIncoming().isModelLocation()) {

            if (device.getDeviceModel() == null || device.getLocation() == null){
                return false;
            }

            String uriPath = eventRoute.getIncoming().getUri().getPath();
            if (uriPath.startsWith("/")) {
                uriPath = uriPath.substring(1);
            }

            String guids[] = uriPath.split("/");
            if (guids.length < 2) {
                LOGGER.warn("Invalid model location URI: {}", uriPath);
                return false;
            }

            Tenant tenant = device.getTenant();
            Application application = device.getApplication();

            // match device model?
            DeviceModel deviceModel = deviceModelRepository.findByTenantIdApplicationNameAndGuid(tenant.getId(), application.getName(), guids[0]);
            if (deviceModel == null || !deviceModel.getGuid().equals(device.getDeviceModel().getGuid())) {
                return false;
            }

            // match location?
            ServiceResponse<Location> locationService = locationSearchService.findByGuid(tenant, application, guids[1]);
            if (!locationService.isOk()) {
                return false;
            }

            Location location = locationService.getResult();
            locationService = locationSearchService.findByName(tenant, application, location.getName(), true);
            if (!locationService.isOk()) {
                return false;
            }

            List<Location> nodes = LocationTreeUtils.getNodesList(locationService.getResult());
            for (Location node : nodes) {
                if (node.getGuid().equals(device.getLocation().getGuid())) {
                    return true;
                }
            }
        }
        
        // match application actor
        if (eventRoute.getIncoming().isApplication()) {
        	return true;
        }

        return false;
    }

    private boolean isFilterExpressionMatch(Event event, EventRoute eventRoute) throws JsonProcessingException {
        Optional<String> expression = Optional.ofNullable(eventRoute.getFilteringExpression())
                .filter(filter -> !filter.isEmpty());

        if (expression.isPresent()) {
            Map<String, Object> objectMap = jsonParsingService.toMap(event.getPayload());
            return evaluationService.evaluateConditional(expression.get(), objectMap);
        } else
            return true;
    }

    private void forwardEvent(EventRoute eventRoute, Event event) throws Exception {
        EventPublisher eventPublisher = (EventPublisher) applicationContext
                .getBean(eventRoute.getOutgoing().getUri().getScheme());
        eventPublisher.send(event, eventRoute.getOutgoing().getUri(),
                eventRoute.getOutgoing().getData(),
                eventRoute.getTenant(),
                eventRoute.getApplication());
        eventRoutePerformedTimes(eventRoute);
    }

    private void logEventFilterMismatch(Event event, EventRoute eventRoute) {
        LOGGER.debug(MessageFormat.format("Dropped route \"{0}\", not matching pattern with content \"{1}\". Message payload: {2} ",
                eventRoute.getName(), eventRoute.getFilteringExpression(), event.getPayload()),
                eventRoute.toURI(),
                eventRoute.getTenant().getLogLevel());
    }

    private void logEventWithInvalidTransformation(Event event, EventRoute eventRoute) {
        LOGGER.debug(MessageFormat.format("Dropped route \"{0}\" with invalid transformation. Message payload: {1} ",
                eventRoute.getName(), event.getPayload()),
                eventRoute.toURI(),
                eventRoute.getTenant().getLogLevel());
    }

    private void eventRoutePerformedTimes(EventRoute eventRoute) {
        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.getByEventRoute(
                eventRoute.getTenant(),
                eventRoute.getApplication(),
                eventRoute);

        EventRouteCounter eventRouteCounter;
        if (response.isOk() && Optional.ofNullable(response.getResult()).isPresent()) {
            eventRouteCounter = response.getResult();
        } else {
            eventRouteCounter = EventRouteCounter.builder()
                    .tenant(eventRoute.getTenant())
                    .application(eventRoute.getApplication())
                    .eventRoute(eventRoute)
                    .performedTimes(0l)
                    .build();
        }

        eventRouteCounter.addPerformedTimes();
        eventRouteCounterService.save(eventRouteCounter.getTenant(),
                eventRouteCounter.getApplication(),
                eventRouteCounter);
        LOGGER.info("Event Route Counter created");
    }
}
