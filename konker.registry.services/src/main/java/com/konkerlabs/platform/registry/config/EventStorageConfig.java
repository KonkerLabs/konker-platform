package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
public class EventStorageConfig {

    private String eventRepositoryBean;

    public EventStorageConfig() {
        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put("eventstorage.bean", EventStorageConfigType.MONGODB.bean());
        Config defaultConf = ConfigFactory.parseMap(defaultMap);
        Config config = ConfigFactory.load().withFallback(defaultConf);
        setEventRepositoryBean(config.getString("eventstorage.bean"));
    }

    public enum EventStorageConfigType {
        MONGODB("mongoEvents"),
        CASSANDRA("cassandraEvents");

        private String bean;

        EventStorageConfigType(String bean) {
            this.bean = bean;
        }

        public String bean(){
            return this.bean;
        }
    }
}
