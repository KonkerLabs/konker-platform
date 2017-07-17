package com.konkerlabs.platform.registry.test.data.base;

import com.github.fakemongo.Fongo;
import com.konkerlabs.platform.registry.data.config.MongoDataConfig;
import com.mongodb.Mongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoTestConfiguration extends MongoDataConfig {

    @Override
    protected String getDatabaseName() {
        return "registry-test";
    }

    @Override
    @Bean
    public Mongo mongo() throws Exception {
        return new Fongo("registry-test").getMongo();
    }
}
