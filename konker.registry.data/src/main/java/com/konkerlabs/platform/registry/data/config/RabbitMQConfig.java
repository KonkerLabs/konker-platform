package com.konkerlabs.platform.registry.data.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Data
@Configuration
public class RabbitMQConfig {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private String hostname;
    private String username;
    private String password;
    private String virtualHost;

    public static final String MSG_HEADER_APIKEY = "apiKey";

    public static final String MSG_HEADER_CHANNEL = "channel";

    public RabbitMQConfig() {
        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put("rabbitmq.hostname", "localhost");
        defaultMap.put("rabbitmq.username", "");
        defaultMap.put("rabbitmq.password", "");
        defaultMap.put("rabbitmq.virtualHost", "");

        Config defaultConf = ConfigFactory.parseMap(defaultMap);
        Config config = ConfigFactory.load().withFallback(defaultConf);

        setHostname(config.getString("rabbitmq.hostname"));
        setUsername(config.getString("rabbitmq.username"));
        setPassword(config.getString("rabbitmq.password"));
        setVirtualHost(config.getString("rabbitmq.virtualHost"));
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        LOGGER.info("Hostname: {}", getHostname());

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(getHostname());
        if (StringUtils.hasText(getUsername())) {
            connectionFactory.setUsername(getUsername());
            connectionFactory.setPassword(getPassword());
        }
        if (StringUtils.hasText(getVirtualHost())) {
            connectionFactory.setVirtualHost(getVirtualHost());
        }
        return connectionFactory;
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        return rabbitTemplate;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory());
        return rabbitAdmin;
    }

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