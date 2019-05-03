package com.konkerlabs.platform.registry.test.data.base;

import com.konkerlabs.platform.registry.data.core.integration.gateway.HttpGateway;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan(basePackages = {
    "com.konkerlabs.platform.registry.business",
        "com.konkerlabs.platform.registry.data.core.security",
    "com.konkerlabs.platform.utilities",
	"com.konkerlabs.platform.registry.audit.repositories",
    "com.konkerlabs.platform.registry.integration.gateways",
        "com.konkerlabs.platform.registry.data.core.integration.converters",
        "com.konkerlabs.platform.registry.data.core.services"
},lazyInit = true)
public class BusinessDataTestConfiguration {

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return mock(RabbitTemplate.class);
    }

    @Bean
    public HttpGateway httpGateway() {
        return mock(HttpGateway.class);
    }

}
