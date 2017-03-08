package com.konkerlabs.platform.registry.test.base;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;
import com.konkerlabs.platform.registry.integration.gateways.SMSMessageGateway;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan(basePackages = {
    "com.konkerlabs.platform.registry.business",
    "com.konkerlabs.platform.utilities",
	"com.konkerlabs.platform.registry.audit.repositories"
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
