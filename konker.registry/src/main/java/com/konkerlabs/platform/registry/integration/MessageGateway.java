package com.konkerlabs.platform.registry.integration;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway(defaultRequestChannel = "konkerMqttOutputChannel",
                  defaultReplyTimeout = "1")
public interface MessageGateway {
    String sendToDevice(String message, @Header(MqttHeaders.TOPIC) String topic);
}
