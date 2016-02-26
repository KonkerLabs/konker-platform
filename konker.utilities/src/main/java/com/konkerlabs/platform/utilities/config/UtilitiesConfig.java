package com.konkerlabs.platform.utilities.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.konkerlabs.platform.utilities", lazyInit = true)
public class UtilitiesConfig {
}
