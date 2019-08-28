package com.konkerlabs.platform.registry.config;

import com.konkerlabs.platform.registry.business.model.Device;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@EnableScheduling
@Data
public class RedisCacheConfig extends CachingConfigurerSupport {

    private static Logger LOG = LoggerFactory.getLogger(RedisCacheConfig.class);

    private String host;
    private Integer port;

    public RedisCacheConfig() {
        LOG.info("Initializing redis caching...");
        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put("redis.master.host", "localhost");
        defaultMap.put("redis.master.port", 6379);
        Config defaultConf = ConfigFactory.parseMap(defaultMap);

        Config config = ConfigFactory.load().withFallback(defaultConf);
        setHost(config.getString("redis.master.host"));
        setPort(config.getInt("redis.master.port"));
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(host);
        factory.setPort(port);
        return factory;
    }

    @Bean("templateRedis")
    public RedisTemplate<Object, Object> redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setExposeConnection(true);
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
        return redisTemplate;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisTemplate templateRedis) {
        RedisCacheManager redisCacheManager = new RedisCacheManager(templateRedis);
        redisCacheManager.setTransactionAware(true);
        redisCacheManager.setLoadRemoteCachesOnStartup(true);
        redisCacheManager.setUsePrefix(true);

        return redisCacheManager;
    }

    @Bean("customKeyGenerator")
    public KeyGenerator customKeyGenerator() {
        return new CustomKeyGenerator();
    }

    @Bean("apiKeyCustomKeyGenerator")
    public KeyGenerator apiKeyCustomKeyGenerator() {
        return new ApiKeyCustomKeyGenerator();
    }

    @Bean("tenantIdDeviceGuidKeyGenerator")
    public KeyGenerator tenantIdDeviceGuidKeyGenerator() {
        return new TenantIdDeviceGuidCustomKeyGenerator();
    }

    @Bean("deviceGuidRemovedKeyGenerator")
    public KeyGenerator DeviceGuidRemovedKeyGenerator() {
        return new DeviceGuidRemovedKeyGenerator();
    }

    @Bean("apiKeyRemovedKeyGenerator")
    public KeyGenerator ApiKeyRemovedKeyGenerator() {
        return new ApiKeyRemovedKeyGenerator();
    }

    @Bean("tenantDeviceGuidRemovedKeyGenerator")
    public KeyGenerator TenantDeviceGuidRemovedKeyGenerator() {
        return new TenantDeviceGuidRemovedKeyGenerator();
    }

    @Bean("tenantApplicationDeviceIdRemovedCustomKeyGenerator")
    public KeyGenerator TenantApplicationDeviceIdRemovedCustomKeyGenerator() {
        return new TenantApplicationDeviceIdRemovedCustomKeyGenerator();
    }

    @Component
    class CustomKeyGenerator implements KeyGenerator {

        @Override
        public Object generate(Object target, Method method, Object... params) {
            if (params.length == 1 && params[0] instanceof Device) {
                Device device = (Device) params[0];
                return MessageFormat.format("CompoundKey[{0}_{1}_{2}]",
                        device.getTenant().getId(),
                        device.getApplication().getName(),
                        device.getDeviceId());
            } else if (method.getName().equals("findByTenantIdAndApplicationAndDeviceId")) {
                return MessageFormat.format("CompoundKey[{0}_{1}_{2}]", params);
            } else {
                StringBuffer pattern = new StringBuffer("CompoundKey[");

                for (int i = 0; i < params.length; i++) {
                    pattern.append("{" + i + "}");
                }
                pattern.append("]");

                return MessageFormat.format(pattern.toString(), params);
            }
        }
    }

    @Component
    class ApiKeyCustomKeyGenerator implements KeyGenerator {

        @Override
        public Object generate(Object target, Method method, Object... params) {
            if (params[0] instanceof Device) {
                Device device = (Device) params[0];
                return MessageFormat.format("CompoundKey[{0}]",
                        device.getApiKey());
            } else {
                StringBuffer pattern = new StringBuffer("CompoundKey[");

                for (int i = 0; i < params.length; i++) {
                    pattern.append("{" + i + "}");
                }
                pattern.append("]");

                return MessageFormat.format(pattern.toString(), params);
            }
        }
    }

    @Component
    class TenantIdDeviceGuidCustomKeyGenerator implements KeyGenerator {

        @Override
        public Object generate(Object target, Method method, Object... params) {
            if (params[0] instanceof Device) {
                Device device = (Device) params[0];
                return MessageFormat.format("CompoundKey[{0}{1}]",
                        device.getTenant().getId(),
                        device.getGuid());
            } else {
                StringBuffer pattern = new StringBuffer("CompoundKey[");

                for (int i = 0; i < params.length; i++) {
                    pattern.append("{" + i + "}");
                }
                pattern.append("]");

                return MessageFormat.format(pattern.toString(), params);
            }
        }
    }



    @Component
    class DeviceGuidRemovedKeyGenerator implements KeyGenerator {

        @Override
        public Object generate(Object target, Method method, Object... params) {
            Device device = (Device) params[0];
            return MessageFormat.format("CompoundKey[{0}]",
                    device.getGuid());
        }
    }

    @Component
    class ApiKeyRemovedKeyGenerator implements KeyGenerator {

        @Override
        public Object generate(Object target, Method method, Object... params) {
            Device device = (Device) params[0];
            return MessageFormat.format("CompoundKey[{0}]",
                    device.getApiKey());
        }
    }

    @Component
    class TenantDeviceGuidRemovedKeyGenerator implements KeyGenerator {

        @Override
        public Object generate(Object target, Method method, Object... params) {
            Device device = (Device) params[0];
            return MessageFormat.format("CompoundKey[{0}{1}]",
                    device.getTenant().getId(),
                    device.getGuid());
        }
    }

    @Component
    class TenantApplicationDeviceIdRemovedCustomKeyGenerator implements KeyGenerator {

        @Override
        public Object generate(Object target, Method method, Object... params) {
            Device device = (Device) params[0];
            return MessageFormat.format("CompoundKey[{0}_{1}_{2}]",
                    device.getTenant().getId(),
                    device.getApplication().getName(),
                    device.getDeviceId());
        }
    }
}
