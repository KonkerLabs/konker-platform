package com.konkerlabs.platform.registry.config;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.util.StringUtils;

import java.util.*;

@Configuration
@EnableMongoRepositories(basePackages = "com.konkerlabs.platform.registry.storage.repositories", mongoTemplateRef = "mongoPrivateStorageTemplate")
@Data
public class MongoPrivateStorageConfig extends AbstractMongoConfiguration {

	private String hostname;
	private Integer port;
	private String username;
	private String password;
	private String dbName = "tenant-db-default";
	private static Logger LOG = LoggerFactory.getLogger(MongoPrivateStorageConfig.class);

    public MongoPrivateStorageConfig() {
    	Map<String, Object> defaultMap = new HashMap<>();
    	defaultMap.put("mongoPrivateStorage.hostname", "localhost");
    	defaultMap.put("mongoPrivateStorage.port", 27017);
    	defaultMap.put("mongoPrivateStorage.username", "");
    	defaultMap.put("mongoPrivateStorage.password", "");
    	Config defaultConf = ConfigFactory.parseMap(defaultMap);

    	Config config = ConfigFactory.load().withFallback(defaultConf);
    	setHostname(config.getString("mongoPrivateStorage.hostname"));
    	setPort(config.getInt("mongoPrivateStorage.port"));
    	setUsername(config.getString("mongoPrivateStorage.username"));
    	setPassword(config.getString("mongoPrivateStorage.password"));

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
		return dbName;
    }


    @Bean(name = "mongoPrivateStorageTemplate")
	public MongoTemplate mongoTemplate(Mongo mongo) throws Exception {
		return new MongoTemplate(mongo, this.getDatabaseName());
	}

	@Override
	@Bean
	public Mongo mongo() throws Exception {
		if (!StringUtils.isEmpty(getUsername()) && !StringUtils.isEmpty(getPassword())) {
			LOG.info("Connecting to MongoDB single node with auth");
			MongoCredential credential = MongoCredential.createCredential(getUsername(), getDatabaseName(), getPassword().toCharArray());
			return new MongoClient(new ServerAddress(hostname), Collections.singletonList(credential));
		} else {
            LOG.info("Connecting to MongoDB locally");
			return new MongoClient(hostname);
		}

	}

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(converters);
    }

}