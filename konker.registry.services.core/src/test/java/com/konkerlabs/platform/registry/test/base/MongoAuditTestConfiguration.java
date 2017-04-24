package com.konkerlabs.platform.registry.test.base;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.fakemongo.Fongo;
import com.konkerlabs.platform.registry.config.MongoAuditConfig;
import com.mongodb.Mongo;

@Configuration
public class MongoAuditTestConfiguration extends MongoAuditConfig {

    @Override
    protected String getDatabaseName() {
        return "logs-test";
    }

    @Override
    @Bean
    public Mongo mongo() throws Exception {
        return new Fongo("logs-test").getMongo();
    }

}
