package com.konkerlabs.platform.registry.test.base;

import com.konkerlabs.platform.registry.business.model.Tenant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebTestConfiguration {

    @Bean
    public Tenant tenant() {
        return Tenant.builder().name("konker").domainName("konker").id("id").build();
    }

}
