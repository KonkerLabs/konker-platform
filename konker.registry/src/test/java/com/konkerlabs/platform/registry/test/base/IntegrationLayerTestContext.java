package com.konkerlabs.platform.registry.test.base;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.konkerlabs.platform.registry.integration")
public class IntegrationLayerTestContext {
}
