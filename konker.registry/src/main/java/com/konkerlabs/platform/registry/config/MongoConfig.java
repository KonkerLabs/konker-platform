package com.konkerlabs.platform.registry.config;

import com.konkerlabs.platform.registry.business.model.converters.InstantReadConverter;
import com.konkerlabs.platform.registry.business.model.converters.InstantWriteConverter;
import com.konkerlabs.platform.registry.business.model.converters.URIReadConverter;
import com.konkerlabs.platform.registry.business.model.converters.URIWriteConverter;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMongoRepositories(basePackages = "com.konkerlabs.platform.registry.business.repositories")
public class MongoConfig extends AbstractMongoConfiguration {

    public static final Config mongoConfig = ConfigFactory.load().getConfig("mongo");

    public static final List<Converter<?,?>> converters = Arrays.asList(
        new Converter[] {
            new InstantReadConverter(),
            new InstantWriteConverter(),
            new URIReadConverter(),
            new URIWriteConverter()
        }
    );

    @Override
    protected String getDatabaseName() {
        return "registry";
    }

    @Override
    public Mongo mongo() throws Exception {
        return new MongoClient(mongoConfig.getString("hostname"),
                Integer.valueOf(mongoConfig.getInt("port"))
        );
    }

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(converters);
    }
}