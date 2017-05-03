package org.konker.registry.cassandraetl.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAutoConfiguration
@EnableAsync
@ComponentScan(basePackages = {
		"com.konkerlabs.platform.registry.business",
		"com.konkerlabs.platform.registry.audit.repositories",
		"com.konkerlabs.platform.utilities"}
)
public class BusinessConfig {
}
