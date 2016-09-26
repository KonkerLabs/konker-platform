package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

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
    public RedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory cf = new JedisConnectionFactory();
        cf.setHostName(config.getString("master.host"));
        cf.setPort(config.getInt("master.port"));
        cf.afterPropertiesSet();
        return cf;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> rt = new RedisTemplate<>();
        rt.setConnectionFactory(redisConnectionFactory());
        return rt;
    }

    /**
     * Create a  message listener container for a specific topic
     * @param redisConnectionFactory
     * @param topic
     * @param messageListener
     * @return RedisMessageListernerContainer
     */
    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            String topic,
            MessageListener messageListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListener, new ChannelTopic(topic));
        return container;
    }



}
