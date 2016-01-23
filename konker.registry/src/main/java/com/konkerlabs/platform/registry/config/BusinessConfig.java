package com.konkerlabs.platform.registry.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.konkerlabs.platform.registry.business")
public class BusinessConfig {
}
