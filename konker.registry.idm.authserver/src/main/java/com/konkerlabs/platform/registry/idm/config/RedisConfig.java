package com.konkerlabs.platform.registry.idm.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;

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

	private String host;
	private Integer port;
    
	public RedisConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("redis.master.host", "localhost");
		defaultMap.put("redis.master.port", 6379);
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setHost(config.getString("redis.master.host"));
		setPort(config.getInt("redis.master.port"));
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