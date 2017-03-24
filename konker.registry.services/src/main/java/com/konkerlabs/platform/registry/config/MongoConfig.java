package com.konkerlabs.platform.registry.config;

import java.util.*;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.konkerlabs.platform.registry.business.model.converters.InstantReadConverter;
import com.konkerlabs.platform.registry.business.model.converters.InstantWriteConverter;
import com.konkerlabs.platform.registry.business.model.converters.URIReadConverter;
import com.konkerlabs.platform.registry.business.model.converters.URIWriteConverter;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@EnableMongoRepositories(basePackages = "com.konkerlabs.platform.registry.business.repositories")
@Data
public class MongoConfig extends AbstractMongoConfiguration {

    private String hostname;
    private Integer port;
    private String username;
    private String password;

    public MongoConfig() {
    	Map<String, Object> defaultMap = new HashMap<>();
    	defaultMap.put("mongo.hostname", "localhost");
    	defaultMap.put("mongo.port", 27017);
    	defaultMap.put("mongo.username", "admin");
    	defaultMap.put("mongo.password", "admin");
    	Config defaultConf = ConfigFactory.parseMap(defaultMap);

    	Config config = ConfigFactory.load().withFallback(defaultConf);
    	setHostname(config.getString("mongo.hostname"));
    	setPort(config.getInt("mongo.port"));
    	setUsername(config.getString("mongo.username"));
    	setPassword(config.getString("mongo.password"));

    }

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
    	MongoCredential credential = MongoCredential.createCredential(getUsername(), getDatabaseName(), getPassword().toCharArray());
    	ServerAddress address = new ServerAddress(getHostname(), getPort());

        return new MongoClient(address, Collections.singletonList(credential));
    }

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(converters);
    }

}