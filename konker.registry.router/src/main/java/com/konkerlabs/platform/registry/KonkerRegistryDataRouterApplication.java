package com.konkerlabs.platform.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KonkerRegistryDataRouterApplication {

    private static final Logger LOG = LoggerFactory.getLogger(KonkerRegistryDataRouterApplication.class);

    public static void main(String[] args) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Start the konker registry data router...");
        }

        SpringApplication.run(KonkerRegistryDataRouterApplication.class, args);
    }

}
