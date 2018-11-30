package com.konkerlabs.platform.registry.router.config;

import com.konkerlabs.platform.registry.data.core.config.RabbitMQDataConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.aopalliance.aop.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Data
@EqualsAndHashCode(callSuper=false)
@Configuration
public class RabbitMQRouterConfig extends RabbitMQDataConfig {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(20);

        Advice[] adviceChain = new Advice[] { interceptor() };
        factory.setAdviceChain(adviceChain);
        return factory;
    }

    @Bean
    public RetryOperationsInterceptor interceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(5)
                .backOffOptions(1000, 2.0, 5000)
                .build();
    }

}