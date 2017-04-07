package com.konkerlabs.platform.registry.api.config;

import com.konkerlabs.platform.registry.config.EventStorageConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class EventStorageApiConfig extends EventStorageConfig {
}
