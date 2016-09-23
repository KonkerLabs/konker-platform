package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Factory for Redis connections
 * Using Jedis as a driver
 * Created by andre on 23/09/16.
 * @author andre
 * @since 2016-09-23
 */
@Configuration
public class RedisConfig {

    public static Config config = ConfigFactory.load().getConfig("redis");

    @Bean
    public JedisConnectionFactory redisFactory() {
        JedisConnectionFactory cf = new JedisConnectionFactory();
        cf.setHostName(config.getString("master.host"));
        cf.setPort(config.getInt("master.port"));
        cf.afterPropertiesSet();
        return cf;
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> rt = new RedisTemplate<>();
        rt.setConnectionFactory(redisFactory());
        return rt;
    }

}
