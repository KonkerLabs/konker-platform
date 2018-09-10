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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

@Service
public class RabbitEventQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitEventQueue.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

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
            ServiceResponse<EventRoute> response = eventRouteService.getByGUID(
                    device.getTenant(),
                    device.getApplication(),
                    eventRouteGuid);

            if (response.isOk()) {
                eventRouteExecutor.execute(event, device, response.getResult());
            }
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
