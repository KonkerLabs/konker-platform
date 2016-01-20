package com.konkerlabs.platform.registry.test.base;

import com.github.fakemongo.Fongo;
import com.konkerlabs.platform.registry.config.MongoConfig;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.mongodb.Mongo;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;

@ContextConfiguration
public class MongoIntegrationTestBase {

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("registry-test");

    @Configuration
    @EnableMongoRepositories(basePackages = "com.konkerlabs.platform.registry.repositories")
    static class MongoConfiguration extends AbstractMongoConfiguration {

        @Override
        protected String getDatabaseName() {
            return "registry-test";
        }

        @Override
        @Bean
        public Mongo mongo() {
            return new Fongo("registry-test").getMongo();
        }

        @Override
        public CustomConversions customConversions() {
            return new CustomConversions(MongoConfig.converters);
        }
    }
}
