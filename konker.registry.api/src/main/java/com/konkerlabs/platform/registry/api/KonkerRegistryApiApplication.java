package com.konkerlabs.platform.registry.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


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
