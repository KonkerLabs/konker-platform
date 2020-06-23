package org.konker.registry.cassandraetl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateEtlConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
