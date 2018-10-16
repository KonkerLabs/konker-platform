package com.konkerlabs.platform.registry.router.integration.eventsqueue;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.core.config.RabbitMQDataConfig;
import com.konkerlabs.platform.registry.data.core.services.routes.api.EventRouteExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class RabbitEventQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitEventQueue.class);

    @Autowired
    private EventRouteExecutor eventRouteExecutor;

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private EventRouteService eventRouteService;

    @RabbitListener(queues = "routed.events")
    public void onReadEvents(Message message) throws Exception {
        MessageProperties properties = message.getMessageProperties();
        Event event = fromByteArray(message.getBody());
        String apiKey = (String) properties.getHeaders().get(RabbitMQDataConfig.MSG_HEADER_APIKEY);
        String eventRouteGuid = (String) properties.getHeaders().get(RabbitMQDataConfig.MSG_HEADER_EVENT_ROUTE_GUID);

        Device device = deviceRegisterService.findByApiKey(apiKey);

        if (device != null) {

            // _echo channel
            if (eventRouteGuid == null &&
                    event.getIncoming().getChannel().equals(EventRouteExecutor.ECHO_CHANNEL)) {
                eventRouteExecutor.execute(event, device, getEchoEvent(event, device));
                return;
            }

            // event route
            ServiceResponse<EventRoute> response = eventRouteService.getByGUID(
                    device.getTenant(),
                    device.getApplication(),
                    eventRouteGuid);

            if (response.isOk()) {
                eventRouteExecutor.execute(event, device, response.getResult());
            }
        }

    }

    private EventRoute getEchoEvent(Event event, Device device) {
        if (event.getIncoming().getChannel().equals(EventRouteExecutor.ECHO_CHANNEL)) {
            Map<String, String> data = new HashMap<>();
            data.put(EventRoute.DEVICE_MQTT_CHANNEL, EventRouteExecutor.ECHO_CHANNEL);

            EventRoute eventRoute = EventRoute
                    .builder()
                    .name(EventRouteExecutor.ECHO_CHANNEL)
                    .incoming(
                            EventRoute.RouteActor
                                    .builder()
                                    .uri(device.toURI())
                                    .data(data)
                                    .build()
                    )
                    .outgoing(
                            EventRoute.RouteActor
                                    .builder()
                                    .uri(device.toURI())
                                    .data(data)
                                    .build()
                    )
                    .tenant(device.getTenant())
                    .application(device.getApplication())
                    .active(true)
                    .build();

            return eventRoute;
        } else {
            return null;
        }
    }

    private Event fromByteArray(byte[] bytes) {
        Event event = null;
        ByteArrayInputStream baos = new ByteArrayInputStream(bytes);

        try {
            ObjectInputStream oos = new ObjectInputStream(baos);
            event = (Event) oos.readObject();

        } catch (Exception e) {
            LOGGER.error("Exception while converting ByteArray to Event...", e);
        }

        return event;
    }

}
