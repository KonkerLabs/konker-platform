package com.konkerlabs.platform.registry.test.data.base;

import com.konkerlabs.platform.registry.data.core.config.RedisConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisDataTestConfiguration extends RedisConfig {

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
