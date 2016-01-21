package com.konkerlabs.platform.registry.config;

import com.konkerlabs.platform.registry.business.model.converters.EventReadConverter;
import com.konkerlabs.platform.registry.business.model.converters.EventWriteConverter;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMongoRepositories(basePackages = "com.konkerlabs.platform.registry.business.repositories")
public class MongoConfig extends AbstractMongoConfiguration {

    public static final List<Converter<?,?>> converters = Arrays.asList(
        new Converter[] {
            new EventWriteConverter(),
            new EventReadConverter()
        }
    );

    @Override
    protected String getDatabaseName() {
        return "registry";
    }

    @Override
    public Mongo mongo() throws Exception {
        return new MongoClient("dev-server.konkerlabs.com");
    }

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(converters);
    }
}