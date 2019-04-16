package com.konkerlabs.platform.registry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KonkerRegistryDataProcessorApplication {

    private static final Logger LOG = LoggerFactory.getLogger(KonkerRegistryDataProcessorApplication.class);

    public static void main(String[] args) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Start the konker registry data processor...");
        }
        
        String profile = System.getenv("DATA_INTERNAL");
		if (StringUtils.isNotBlank(profile)) {
			LOG.info("Data Internal is enable!!!");
			System.setProperty("spring.profiles.active", profile);
		}
        
        SpringApplication.run(KonkerRegistryDataProcessorApplication.class, args);
    }

}
