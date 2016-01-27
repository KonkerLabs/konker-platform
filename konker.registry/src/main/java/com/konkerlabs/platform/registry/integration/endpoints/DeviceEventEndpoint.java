package com.konkerlabs.platform.registry.integration.endpoints;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

@MessageEndpoint
public class DeviceEventEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceEventEndpoint.class);

    private DeviceEventService deviceEventService;

    @Autowired
    public DeviceEventEndpoint(DeviceEventService deviceEventService) {
        this.deviceEventService = deviceEventService;
    }

    @ServiceActivator(inputChannel = "konkerMqttInputChannel")
    public void onEvent(Message<String> message) throws MessagingException {

        Object topic = message.getHeaders().get(MqttHeaders.TOPIC);
        if (topic == null || topic.toString().isEmpty()) {
            throw new MessagingException(message,"Topic cannot be null or empty");
        }

        String deviceId = extractDeviceId(topic.toString());
        if (deviceId == null) {
            throw new MessagingException(message,"Device ID cannot be retrieved");
        }

        try {
            deviceEventService.logEvent(message.getPayload(), deviceId);
        } catch (BusinessException e) {
            throw new MessagingException(message,e);
        }
    }

    private String extractDeviceId(String topic) {
        try {
            return topic.split("/")[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
