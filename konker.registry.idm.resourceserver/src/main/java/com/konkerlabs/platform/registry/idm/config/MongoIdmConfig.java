package com.konkerlabs.platform.registry.idm.config;

import com.konkerlabs.platform.registry.config.MongoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.convert.CustomConversions;


@EnableAutoConfiguration
public class MongoIdmConfig extends MongoConfig {

    private Logger LOG = LoggerFactory.getLogger(MongoIdmConfig.class);

    public MongoIdmConfig(){
        LOG.debug("Init mongo idm config...");
    }

    @Override
    @Bean
    public CustomConversions customConversions() {
        return super.customConversions();
    }
}
