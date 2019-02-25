package com.konkerlabs.platform.registry.config;

import java.util.*;
import java.util.stream.Collectors;

import com.mongodb.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.konkerlabs.platform.registry.business.model.converters.InstantReadConverter;
import com.konkerlabs.platform.registry.business.model.converters.InstantWriteConverter;
import com.konkerlabs.platform.registry.business.model.converters.URIReadConverter;
import com.konkerlabs.platform.registry.business.model.converters.URIWriteConverter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


@Configuration
@EnableMongoRepositories(basePackages = "com.konkerlabs.platform.registry.storage.repositories", mongoTemplateRef = "mongoPrivateStorageTemplate")
@Data
public class MongoPrivateStorageConfig extends AbstractMongoConfiguration {

	private List<ServerAddress> hostname = new ArrayList<>();
	private Integer port;
	private String username;
	private String password;
	private String dbName = "private-storage";
	private static Logger LOG = LoggerFactory.getLogger(MongoPrivateStorageConfig.class);

    public MongoPrivateStorageConfig() {
    	Map<String, Object> defaultMap = new HashMap<>();
    	defaultMap.put("mongoPrivateStorage.hostname", "localhost");
    	defaultMap.put("mongoPrivateStorage.port", 27017);
    	defaultMap.put("mongoPrivateStorage.username", "");
    	defaultMap.put("mongoPrivateStorage.password", "");
    	Config defaultConf = ConfigFactory.parseMap(defaultMap);

    	Config config = ConfigFactory.load().withFallback(defaultConf);

		setUsername(Optional.ofNullable(config.getString("mongoPrivateStorage.username")).isPresent()
				? config.getString("mongoPrivateStorage.username") : null);
		setPassword(Optional.ofNullable(config.getString("mongoPrivateStorage.password")).isPresent()
				? config.getString("mongoPrivateStorage.password") : null);

		List<String> seedList = Optional.ofNullable(config.getString("mongoPrivateStorage.hostname")).isPresent() ?
				Arrays.asList(config.getString("mongoPrivateStorage.hostname").split(",")) : null;

		setPort(Optional.ofNullable(config.getInt("mongoPrivateStorage.port")).isPresent()
			? config.getInt("mongoPrivateStorage.port") : null);

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
		MongoTemplate mongoTemplate = new MongoTemplate(mongo, this.getDatabaseName());
		//createUserIfNotExists();
		return mongoTemplate;
	}

	@Override
	@Bean(name = "mongoPrivateStorage")
	public Mongo mongo() throws Exception {
		if (!StringUtils.isEmpty(getUsername()) && !StringUtils.isEmpty(getPassword())) {
			try {
				LOG.info("Connecting to MongoDB single node with auth");
				MongoCredential credential = MongoCredential.createCredential(getUsername(), getDatabaseName(), getPassword().toCharArray());
				return new MongoClient(hostname, Collections.singletonList(credential));
			} catch (Exception e) {
				return new MongoClient(hostname);
			}
		} else {
			LOG.info("Connecting to MongoDB locally");
			return new MongoClient(hostname);
		}
	}

	private void createUserIfNotExists() throws Exception {
		if (!username.isEmpty()
				&& !password.isEmpty()) {
			LOG.info("Caling the method createUserIfNoExists");

			Mongo mongo = mongo();
			DB db = mongo.getDB(getDatabaseName());

			BasicDBObject dbStats = new BasicDBObject("usersInfo", 1);
			CommandResult statComand = db.command(dbStats);
			BasicDBList users = (BasicDBList) statComand.get("users");
			List<String> allUsers = users.stream()
					.map(u -> (String) ((BasicDBObject) u).get("user"))
					.collect(Collectors.toList());
			LOG.info("List all users: " + allUsers);
			if (!allUsers.contains(username)) {
				LOG.info("Creating user: " + username);
				Map<String, Object> commandArguments = new HashMap<>();
				commandArguments.put("createUser", username);
				commandArguments.put("pwd", password);
				commandArguments.put("roles", new String[]{ "readWrite" });
				BasicDBObject command = new BasicDBObject(commandArguments);
				db.command(command);
			}
		}
	}

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(converters);
    }

}