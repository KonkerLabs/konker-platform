package com.konkerlabs.platform.registry.test.base;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = "com.konkerlabs.platform.registry.integration", lazyInit = true)
public class IntegrationLayerTestContext {

    @Bean
    public RestTemplate enrichmentRestTemplate() {
        return Mockito.mock(RestTemplate.class);
    }
}
