package com.konkerlabs.platform.registry.test.data.core.base;

import com.konkerlabs.platform.registry.data.core.config.IntegrationConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan(basePackages = "com.konkerlabs.platform.registry.integration", lazyInit = true)
public class IntegrationTestConfiguration extends IntegrationConfig {

    @Override
    @Bean
    public RestTemplate enrichmentRestTemplate() {
        return mock(RestTemplate.class);
    }

}