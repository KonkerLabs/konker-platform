package org.konker.registry.cassandraetl.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.konkerlabs.platform.registry.config.MongoConfig;

@Configuration
@EnableAutoConfiguration
@EnableMongoRepositories(basePackages = {"com.konkerlabs.platform.registry.business.repositories.events"
})
public class MongoEtlConfig extends MongoConfig {

}