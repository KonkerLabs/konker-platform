package com.konkerlabs.platform.registry.test.base;

import com.github.fakemongo.Fongo;
import com.konkerlabs.platform.registry.config.MongoAuditConfig;
import com.konkerlabs.platform.registry.config.MongoPrivateStorageConfig;
import com.mongodb.Mongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoPrivateStorageTestConfiguration extends MongoPrivateStorageConfig {

    @Override
    protected String getDatabaseName() {
        return "konker";
    }

    @Override
    @Bean(name = "mongoPrivateStorage")
    public Mongo mongo() throws Exception {
        return new Fongo("konker").getMongo();
    }

}
