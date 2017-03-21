package com.konkerlabs.platform.registry.test.data.base;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.konkerlabs.platform.registry.data.config.RedisConfig;

@Configuration
public class RedisTestConfiguration extends RedisConfig {

    @Override
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return super.redisConnectionFactory();
    }

    @Override
    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        return super.redisTemplate();

    }


}
