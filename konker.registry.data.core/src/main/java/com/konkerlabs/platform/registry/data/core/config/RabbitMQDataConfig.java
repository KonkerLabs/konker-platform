package com.konkerlabs.platform.registry.data.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.konkerlabs.platform.registry.config.RabbitMQConfig;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Configuration
public class RabbitMQDataConfig extends RabbitMQConfig {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Bean
    public Queue dataPubQueue() {

       boolean durable = true;
       boolean exclusive = false;
       boolean autoDelete = false;

       return new Queue("data.pub", durable, exclusive, autoDelete);

    }

    @Bean
    public Queue configPubQueue() {

       boolean durable = true;
       boolean exclusive = false;
       boolean autoDelete = false;

       return new Queue("mgmt.config.pub", durable, exclusive, autoDelete);

    }

}