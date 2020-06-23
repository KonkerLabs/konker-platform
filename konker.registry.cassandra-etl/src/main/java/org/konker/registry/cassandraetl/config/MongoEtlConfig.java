package org.konker.registry.cassandraetl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.konkerlabs.platform.registry.config.MongoConfig;

@Configuration
@EnableMongoRepositories(basePackages = {
        "com.konkerlabs.platform.registry.business.repositories",
        "com.konkerlabs.platform.registry.billing.repositories",
        "org.konker.registry.cassandraetl.repositories"
})
public class MongoEtlConfig extends MongoConfig {

}