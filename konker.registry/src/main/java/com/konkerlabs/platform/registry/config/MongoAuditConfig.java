package com.konkerlabs.platform.registry.config;

import java.util.Arrays;
import java.util.List;

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
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Configuration
@EnableMongoRepositories(basePackages = "com.konkerlabs.platform.registry.audit.repositories", mongoTemplateRef = "mongoAuditTemplate")
public class MongoAuditConfig extends AbstractMongoConfiguration {

	public static final Config mongoAuditConfig = ConfigFactory.load().getConfig("mongoAudit");

    public static final List<Converter<?,?>> converters = Arrays.asList(
        new Converter[] {
					new InstantReadConverter(), new InstantWriteConverter(), new URIReadConverter(),
					new URIWriteConverter()
        }
    );

    @Override
    protected String getDatabaseName() {
		return "logs";
    }

    @Override
	@Bean(name = "mongoAuditTemplate")
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(this.mongo(), this.getDatabaseName());
	}

	@Override
    public Mongo mongo() throws Exception {
		return new MongoClient(mongoAuditConfig.getString("hostname"),
                Integer.valueOf(mongoAuditConfig.getInt("port"))
        );
    }

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(converters);
    }

}