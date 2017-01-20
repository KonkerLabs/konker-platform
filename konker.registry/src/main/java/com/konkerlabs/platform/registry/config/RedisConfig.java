package com.konkerlabs.platform.registry.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

/**
 * Factory for Redis connections
 * Using Jedis as a driver
 * Created by andre on 23/09/16.
 * @author andre
 * @since 2016-09-23
 */
@Configuration
@Data
public class RedisConfig {

//    public static Config config = ConfigFactory.load().getConfig("redis");
	private String host;
	private Integer port;
    
    public RedisConfig() {
    	if (ConfigFactory.load().hasPath("redis")) {
    		Config config = ConfigFactory.load().getConfig("redis");
    		setHost(config.getString("master.host"));
    		setPort(config.getInt("master.port"));
    	} else {
    		setHost("localhost");
    		setPort(6379);
    	}
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory cf = new JedisConnectionFactory();
        cf.setHostName(getHost());
        cf.setPort(getPort());
        cf.afterPropertiesSet();
        return cf;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        StringRedisTemplate rt = new StringRedisTemplate();
        rt.setConnectionFactory(redisConnectionFactory());
        return rt;
    }
}