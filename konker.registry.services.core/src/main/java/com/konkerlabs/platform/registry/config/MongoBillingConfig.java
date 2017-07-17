package com.konkerlabs.platform.registry.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
@EnableMongoRepositories(basePackages = "com.konkerlabs.platform.registry.billing.repositories", mongoTemplateRef = "mongoBillingTemplate")
@Data
public class MongoBillingConfig extends AbstractMongoConfiguration {

	private List<ServerAddress> hostname = new ArrayList<>();
	private Integer port;
	private String username;
	private String password;
	private static Logger LOG = LoggerFactory.getLogger(MongoConfig.class);
    
    public MongoBillingConfig() {
    	Map<String, Object> defaultMap = new HashMap<>();
    	defaultMap.put("mongoBilling.hostname", "localhost");
    	defaultMap.put("mongoBilling.port", 27017);
    	defaultMap.put("mongoBilling.username", "");
    	defaultMap.put("mongoBilling.password", "");
    	Config defaultConf = ConfigFactory.parseMap(defaultMap);

    	Config config = ConfigFactory.load().withFallback(defaultConf);
    	setPort(config.getInt("mongoBilling.port"));
    	setUsername(config.getString("mongoBilling.username"));
    	setPassword(config.getString("mongoBilling.password"));

		List<String> seedList = Optional.ofNullable(config.getString("mongoBilling.hostname")).isPresent() ?
				Arrays.asList(config.getString("mongoBilling.hostname").split(",")) : null;

		for (String seed : seedList) {
			try {
				hostname.add(new ServerAddress(seed, port));
			} catch (Exception e) {
				LOG.error("Error constructing mongo factory", e);
			}
		}

	}

    public static final List<Converter<?,?>> converters = Arrays.asList(
        new Converter[] {
					new InstantReadConverter(), new InstantWriteConverter(), new URIReadConverter(),
					new URIWriteConverter()
        }
    );

    @Override
    protected String getDatabaseName() {
		return "billing";
    }

    @Override
	@Bean(name = "mongoBillingTemplate")
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(this.mongo(), this.getDatabaseName());
	}


	@Override
	public Mongo mongo() throws Exception {
		if (!StringUtils.isEmpty(getUsername()) && !StringUtils.isEmpty(getPassword())) {
			try {
				MongoCredential credential = MongoCredential.createCredential(getUsername(), getDatabaseName(), getPassword().toCharArray());
				return new MongoClient(hostname, Collections.singletonList(credential));
			} catch (Exception e) {
				return new MongoClient(hostname);
			}
		} else {
			return new MongoClient(hostname);
		}

	}

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(converters);
    }

}