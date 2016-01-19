package com.konkerlabs.platform.registry.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.konkerlabs.platform.registry.repositories")
public class DatabaseConfig extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "registry";
    }

    @Override
    public Mongo mongo() throws Exception {
        return new MongoClient("dev-server.konkerlabs.com");
    }

//    @Bean
//    public MongoDbFactory mongoDbFactory() throws Exception {
//        return new SimpleMongoDbFactory(new MongoClient("dev-server.konkerlabs.com"), "registry");
//    }
//
//    @Bean
//    public MongoTemplate mongoTemplate() throws Exception {
//        return new MongoTemplate(mongoDbFactory());
//    }
}