package com.konkerlabs.platform.registry.integration.gateways;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway(defaultRequestChannel = "konkerMqttOutputChannel",
                  defaultReplyTimeout = "1")
public interface MqttMessageGateway {
    String send(String message, @Header(MqttHeaders.TOPIC) String topic);
}
