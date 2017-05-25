package com.konkerlabs.platform.registry.test.data.base;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan(basePackages = {
    "com.konkerlabs.platform.registry.business",
    "com.konkerlabs.platform.registry.data.services",
    "com.konkerlabs.platform.registry.data.context",
    "com.konkerlabs.platform.utilities",
	"com.konkerlabs.platform.registry.audit.repositories"
},lazyInit = true)
public class BusinessTestConfiguration {

    @Bean
    public MqttMessageGateway mqttMessageGateway() {
        return mock(MqttMessageGateway.class);
    }

    @Bean
    public HttpGateway httpGateway() {
        return mock(HttpGateway.class);
    }

}
