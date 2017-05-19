package com.konkerlabs.platform.registry.integration.endpoints;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

@MessageEndpoint
public class DeviceEventMqttEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceEventMqttEndpoint.class);

    private DeviceEventProcessor deviceEventProcessor;

    private Pattern subPattern = Pattern.compile("pub/(.*?)/(.*)");
    private Pattern subDataPattern = Pattern.compile("data/(.*?)/pub/(.*)");

    @Autowired
    public DeviceEventMqttEndpoint(DeviceEventProcessor deviceEventProcessor) {
        this.deviceEventProcessor = deviceEventProcessor;
    }

    @ServiceActivator(inputChannel = "konkerMqttInputChannel")
    public void onEvent(Message<String> message) throws MessagingException {

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("A message has arrived -> " + message.toString());

        Object topic = message.getHeaders().get(MqttHeaders.TOPIC);
        if (topic == null || topic.toString().isEmpty()) {
            throw new MessagingException(message,"Topic cannot be null or empty");
        }

        try {
            String apiKey = null;
            String channel = null;
            String payload = message.getPayload();

            Matcher subMatcher = subPattern.matcher(topic.toString());

            if (!subMatcher.matches()) {
                subMatcher = subDataPattern.matcher(topic.toString());
            }

            if (subMatcher.matches()) {
                apiKey = subMatcher.group(1);
                channel = subMatcher.group(2);

                deviceEventProcessor.process(apiKey, channel, payload);
            } else {

                LOGGER.error("Invalid topic: " + topic.toString());
            }

        } catch (BusinessException be) {
            LOGGER.error(message.getPayload(), be);
        }
    }

}
