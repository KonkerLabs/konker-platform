package com.konkerlabs.platform.registry.integration.endpoints;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.data.config.RabbitMQDataConfig;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DeviceEventRabbitEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceEventRabbitEndpoint.class);

    private DeviceEventProcessor deviceEventProcessor;

    @Autowired
    public DeviceEventRabbitEndpoint(DeviceEventProcessor deviceEventProcessor) {
        this.deviceEventProcessor = deviceEventProcessor;
    }

    @RabbitListener(queues = "data.pub")
    public void onDataPub(Message message) {

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("A message has arrived -> " + message);

        MessageProperties properties = message.getMessageProperties();
        if (properties == null || properties.getHeaders().isEmpty()) {
            LOGGER.error("MessageProperties is empty");
            return;
        }

        String apiKey = (String) properties.getHeaders().get(RabbitMQDataConfig.MSG_HEADER_APIKEY);
        String channel = (String) properties.getHeaders().get(RabbitMQDataConfig.MSG_HEADER_CHANNEL);
        Long epochMilli = (Long) properties.getHeaders().get(RabbitMQDataConfig.MSG_HEADER_TIMESTAMP);
        Instant ingestedTimestamp = null;

        byte[] bytesPayload = message.getBody();

        if (!StringUtils.hasText(apiKey)) {
            LOGGER.error("Apikey not found.");
            return;
        }
        if (!StringUtils.hasText(channel)) {
            LOGGER.error("Channel not found.");
            return;
        }
        if (epochMilli != null) {
            ingestedTimestamp = Instant.ofEpochMilli(epochMilli);
        } else {
        	LOGGER.error("ts rabbit not found.");
        	ingestedTimestamp = Instant.now();
        }

        try {
            deviceEventProcessor.process(apiKey, channel, bytesPayload, ingestedTimestamp);
        } catch (BusinessException be) {
            LOGGER.error("BusinessException processing message", be);
        }

    }

}
