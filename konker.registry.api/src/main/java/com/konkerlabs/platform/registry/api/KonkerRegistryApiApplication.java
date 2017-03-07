package com.konkerlabs.platform.registry.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KonkerRegistryApiApplication {

    private static final Logger LOG = LoggerFactory.getLogger(KonkerRegistryApiApplication.class);

    public static void main(String[] args) {
        if(LOG.isInfoEnabled()){
            LOG.info("Start the konker registry api...");
        }
        SpringApplication.run(KonkerRegistryApiApplication.class, args);
    }
}
