package com.konkerlabs.platform.registry.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KonkerRegistryDataApiApplication {

    private static final Logger LOG = LoggerFactory.getLogger(KonkerRegistryDataApiApplication.class);

    public static void main(String[] args) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Start the konker registry data...");
        }
        SpringApplication.run(KonkerRegistryDataApiApplication.class, args);
    }

}
