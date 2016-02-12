package com.konkerlabs.platform.registry.integration.processors;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceEventProcessor.class);

    private EventRuleExecutor eventRuleExecutor;

    private DeviceEventService deviceEventService;

    @Autowired
    public DeviceEventProcessor(DeviceEventService deviceEventService, EventRuleExecutor eventRuleExecutor) {
        this.deviceEventService = deviceEventService;
        this.eventRuleExecutor = eventRuleExecutor;
    }

    public void process(String topic, String payload) throws BusinessException {

        String deviceId = extractFromTopicLevel(topic, 2);
        if (deviceId == null) {
            throw new BusinessException("Device ID cannot be retrieved");
        }

        String incomingChannel = extractFromTopicLevel(topic, 3);
        if (incomingChannel == null) {
            throw new BusinessException("Event incoming channel cannot be retrieved");
        }

        Event event = Event.builder()
                .channel(incomingChannel)
                .payload(payload)
                .build();

        deviceEventService.logEvent(event, deviceId);

        try {
            eventRuleExecutor.execute(event, new URI("device",deviceId,null,null,null));
        } catch (URISyntaxException e) {
            LOGGER.error("URI syntax error. Probably wrong device ID.", e);
        }
    }

    private String extractFromTopicLevel(String channel, int index) {
        try {
            return channel.split("/")[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
