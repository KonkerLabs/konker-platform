package com.konkerlabs.platform.registry.test.base;

import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;
import com.konkerlabs.platform.registry.integration.gateways.SMSMessageGateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan(basePackages = {
    "com.konkerlabs.platform.registry.business",
    "com.konkerlabs.platform.utilities",
},lazyInit = true)
public class BusinessTestConfiguration {

    @Bean
    public MqttMessageGateway mqttMessageGateway() {
        return mock(MqttMessageGateway.class);
    }
    
    @Bean
    public SMSMessageGateway smsMessageGateway() {
        return mock(SMSMessageGateway.class);
    }

    @Bean
    public HttpGateway httpEnrichmentGateway() {
        return mock(HttpGateway.class);
    }
}
