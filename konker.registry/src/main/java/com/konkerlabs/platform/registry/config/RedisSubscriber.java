package com.konkerlabs.platform.registry.config;


import lombok.Builder;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.function.Function;

/**
 * Helper for subscribing in a redis topic
 */
@Builder
public class RedisSubscriber {
    private JedisPubSub jedisPubSub;
    private String topic;
    private Instant startAt;
    private RedisTemplate<String, String> redisTemplate;
    private Function function;

    @PostConstruct
    public void subscribe(){
        startAt = Instant.now();
        Jedis jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                this.unsubscribe();
                jedisPubSub.onMessage(channel, message);
            }
        }, topic);
    }

    @PreDestroy
    public void unsubscribe(){
        if(jedisPubSub.isSubscribed()){
            jedisPubSub.unsubscribe();
        }
    }
}