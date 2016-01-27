package com.konkerlabs.platform.registry.test.base;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class IntegrationLayerTestContext {

    @Configuration
    @ComponentScan(basePackages = "com.konkerlabs.platform.registry.integration")
    static class IntegrationLayerConfiguration {}
}
