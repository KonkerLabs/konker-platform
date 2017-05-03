package org.konker.registry.cassandraetl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.konkerlabs.platform.registry.config.MongoConfig;

@Configuration
@EnableMongoRepositories(basePackages = {
        "com.konkerlabs.platform.registry.business.repositories"
})
public class MongoEtlConfig extends MongoConfig {

}