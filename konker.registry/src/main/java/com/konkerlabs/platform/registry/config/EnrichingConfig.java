package com.konkerlabs.platform.registry.config;

import com.konkerlabs.platform.registry.integration.gateways.HttpEnrichmentGateway;
import com.konkerlabs.platform.registry.integration.gateways.HttpEnrichmentGatewayImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class EnrichingConfig {

    @Bean(name = "enrichmentRestTemplate")
    public RestTemplate enrichmentRestTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "httpEnrichmentGateway")
    public HttpEnrichmentGateway httpEnrichmentGateway() {
        HttpEnrichmentGatewayImpl httpEnrichmentGateway = new HttpEnrichmentGatewayImpl();
        httpEnrichmentGateway.setRestTemplate(this.enrichmentRestTemplate());

        return httpEnrichmentGateway;
    }

}
