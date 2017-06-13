package com.konkerlabs.platform.registry.integration.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class KonkerEurekaServer {

    public static void main(String[] args) {
        SpringApplication.run(KonkerEurekaServer.class);
    }
}
