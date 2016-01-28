package com.konkerlabs.platform.registry.integration.processors;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceEventProcessor.class);

    private MqttMessageGateway mqttMessageGateway;
    private DeviceEventService deviceEventService;

    @Autowired
    public DeviceEventProcessor(DeviceEventService deviceEventService, MqttMessageGateway mqttMessageGateway) {
        this.deviceEventService = deviceEventService;
        this.mqttMessageGateway = mqttMessageGateway;
    }

    public void process(String channel, String payload) throws BusinessException {

        String deviceId = extractChannelLevel(channel, 2);
        if (deviceId == null) {
            throw new BusinessException("Device ID cannot be retrieved");
        }

        Event event = Event.builder()
                .channel(channel)
                .payload(payload)
                .build();

        deviceEventService.logEvent(event, deviceId);

        //FIXME Refactor this to support arbitrary command execution in an well designed way
        String action = extractChannelLevel(channel,3);
        if ("command".equals(action)) {
            String destinationDevice = payload.substring(0,16);
            String destinationTopic = MessageFormat.format("konker/device/{0}/in",destinationDevice);
            mqttMessageGateway.send(payload,destinationTopic);
        }
    }

    private String extractChannelLevel(String channel, int index) {
        try {
            return channel.split("/")[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
