package com.konkerlabs.platform.registry.test.base;

import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan(basePackages = "com.konkerlabs.platform.registry.business")
public class BusinessTestConfiguration {

    @Autowired
    MqttMessageGateway mqttMessageGateway;

    @Bean
    public MqttMessageGateway mqttMessageGateway() {
        return mock(MqttMessageGateway.class);
    }
}
