package com.konkerlabs.platform.registry.data.core.integration.gateway;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.config.RabbitMQConfig;
import com.konkerlabs.platform.registry.data.core.config.RabbitMQDataConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.MessageFormat;

@Service
public class RabbitGateway {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitGateway.class);

    public void sendEvent(String apiKey, String channel, byte[] payload) {

        try {
            MessageProperties properties = new MessageProperties();
            properties.setHeader(RabbitMQConfig.MSG_HEADER_APIKEY, apiKey);
            properties.setHeader(RabbitMQConfig.MSG_HEADER_CHANNEL, channel);

            Message message = new Message(payload, properties);

            rabbitTemplate.convertAndSend("data.sub", message);
        } catch (AmqpException ex) {
            LOGGER.error("AmqpException while sending message to RabbitMQ...", ex);
        }
    }

    public void sendConfig(String apiKey, String config) {

        try {
            MessageProperties properties = new MessageProperties();
            properties.setHeader(RabbitMQConfig.MSG_HEADER_APIKEY, apiKey);

            Message message = new Message(config.getBytes("UTF-8"), properties);

            rabbitTemplate.convertAndSend("mgmt.config.sub", message);
        } catch (AmqpException | UnsupportedEncodingException ex) {
            LOGGER.error("AmqpException while sending message to RabbitMQ...", ex);
        }

    }

    public void queueEvent(Device device, EventRoute eventRoute, Event event) {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setHeader(RabbitMQDataConfig.MSG_HEADER_APIKEY, device.getApiKey());
            properties.setHeader(RabbitMQDataConfig.MSG_HEADER_EVENT_ROUTE_GUID, eventRoute.getGuid());

            Message message = new Message(toByteArray(event), properties);
            rabbitTemplate.convertAndSend("routed.events", message);

            if (!event.getIncoming().getChannel().equals("_echo")) {
                LOGGER.info(MessageFormat.format("Output tenant: {0} app: {1} channel: {2} device: {3} ts: {4} size: {5}",
                        device.getTenant().getDomainName(),
                        device.getApplication().getName(),
                        eventRoute.getOutgoing().getData().get("channel"),
                        eventRoute.getOutgoing().getDisplayName(),
                        event.getCreationTimestamp(),
                        event.getPayload().getBytes().length));

            }

        } catch (AmqpException ex) {
            LOGGER.error("AmqpException while sending message to RabbitMQ...", ex);
        }
    }

    public void queueEventDataPub(String apiKey, String channel, Long epochMilli, byte[] payload) {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setHeader(RabbitMQConfig.MSG_HEADER_APIKEY, apiKey);
            properties.setHeader(RabbitMQConfig.MSG_HEADER_CHANNEL, channel);
            properties.setHeader(RabbitMQConfig.MSG_HEADER_TIMESTAMP, epochMilli);

            Message message = new Message(payload, properties);

            rabbitTemplate.convertAndSend("data.pub", message);
        } catch (AmqpException ex) {
            LOGGER.error("AmqpException while sending message to RabbitMQ...", ex);
        }
    }

    private byte[] toByteArray(Event event) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(event);
            oos.flush();

        } catch (Exception e) {
            LOGGER.error("Exception while converting Event to ByteArray...", e);
        }

        return baos.toByteArray();
    }
}
