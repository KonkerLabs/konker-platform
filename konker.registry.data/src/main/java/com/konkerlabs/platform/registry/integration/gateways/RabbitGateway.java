package com.konkerlabs.platform.registry.integration.gateways;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.data.config.RabbitMQConfig;

@Service
public class RabbitGateway {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitGateway.class);

    public void sendEvent(String apiKey, String channel, String payload) {

        try {
            MessageProperties properties = new MessageProperties();
            properties.setHeader(RabbitMQConfig.MSG_HEADER_APIKEY, apiKey);
            properties.setHeader(RabbitMQConfig.MSG_HEADER_CHANNEL, channel);

            Message message = new Message(payload.getBytes("UTF-8"), properties);

            rabbitTemplate.convertAndSend("data.sub", message);
        } catch (AmqpException | UnsupportedEncodingException ex) {
            LOGGER.error("AmqpException while sending message to RabbitMQ...", ex);
        }
    }

    public void sendConfig(String apiKey, String config) {

        try {
            MessageProperties properties = new MessageProperties();
            properties.setHeader(RabbitMQConfig.MSG_HEADER_APIKEY, apiKey);

            Message message = new Message("".getBytes("UTF-8"), properties);

            rabbitTemplate.convertAndSend("mgmt.config.sub", message);
        } catch (AmqpException | UnsupportedEncodingException ex) {
            LOGGER.error("AmqpException while sending message to RabbitMQ...", ex);
        }

    }

}
