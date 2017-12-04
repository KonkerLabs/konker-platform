package com.konkerlabs.platform.registry.alerts.config;

import com.konkerlabs.platform.registry.config.MongoConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableAutoConfiguration
@EnableMongoRepositories(basePackages = "com.konkerlabs.platform.registry.alerts.repositories")
public class MongoAlertsConfig extends MongoConfig {

}