package com.konkerlabs.platform.registry.test.data.base;

import com.github.fakemongo.Fongo;
import com.konkerlabs.platform.registry.data.core.config.MongoDataCoreConfig;
import com.mongodb.Mongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDataTestConfiguration extends MongoDataCoreConfig {

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
