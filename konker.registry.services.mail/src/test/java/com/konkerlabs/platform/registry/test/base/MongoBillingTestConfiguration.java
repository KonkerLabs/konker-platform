package com.konkerlabs.platform.registry.test.base;

import com.github.fakemongo.Fongo;
import com.mongodb.Mongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoBillingTestConfiguration extends MongoBillingConfig {

    @Override
    protected String getDatabaseName() {
        return "billing-test";
    }

    @Override
    @Bean
    public Mongo mongo() throws Exception {
        return new Fongo("billing-test").getMongo();
    }
}
