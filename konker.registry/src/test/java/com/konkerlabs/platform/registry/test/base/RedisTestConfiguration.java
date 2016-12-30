package com.konkerlabs.platform.registry.test.base;

import com.konkerlabs.platform.registry.config.RedisConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

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
