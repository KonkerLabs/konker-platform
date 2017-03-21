package com.konkerlabs.platform.registry.data.config;

import com.konkerlabs.platform.registry.config.EventStorageConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventsDataStorageConfig {

    @Bean
    public EventStorageConfig getStorageConfig(){
        return new EventStorageConfig();
    }
}
