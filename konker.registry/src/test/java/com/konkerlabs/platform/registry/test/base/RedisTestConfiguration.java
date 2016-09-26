package com.konkerlabs.platform.registry.test.base;

import com.konkerlabs.platform.registry.config.RedisConfig;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by andre on 23/09/16.
 */
@Configuration
public class RedisTestConfiguration extends RedisConfig {

    @Override
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return Mockito.mock(
                JedisConnectionFactory.class,
                Mockito.withSettings().extraInterfaces(DisposableBean.class));
    }

    @Override
    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RedisTemplate<String, Object> redisTemplate() {
        return super.redisTemplate();
    }


}
