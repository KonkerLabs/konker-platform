package com.konkerlabs.platform.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KonkerIdmServerApplication {

    private static final Logger LOG = LoggerFactory.getLogger(KonkerIdmServerApplication.class);

    public static void main(String[] args) {
        if(LOG.isInfoEnabled()){
            LOG.info("Start the konker registry IDM...");
        }
        SpringApplication.run(KonkerIdmServerApplication.class, args);
    }
}
