package com.konkerlabs.platform.registry.api.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.fakemongo.Fongo;
import com.konkerlabs.platform.registry.api.config.MongoApiConfig;
import com.mongodb.Mongo;

@Configuration
public class MongoTestConfig extends MongoApiConfig {

    @Override
    protected String getDatabaseName() {
        return "registry-test";
    }

    @Override
    @Bean
    public Mongo mongo() {
        return new Fongo("registry-test").getMongo();
    }
}
