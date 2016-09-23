package com.konkerlabs.platform.registry.test.base;

import com.konkerlabs.platform.registry.config.RedisConfig;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by andre on 23/09/16.
 */
@Configuration
public class RedisTestConfiguration extends RedisConfig {

    @Override
    @Bean
    public JedisConnectionFactory redisFactory() {
        return Mockito.mock(
                JedisConnectionFactory.class,
                Mockito.withSettings().extraInterfaces(DisposableBean.class));
    }

    @Override
    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        return super.redisTemplate();
    }

    @Test
    public void shouldGetAValidRedisConnection() {

    }

}
