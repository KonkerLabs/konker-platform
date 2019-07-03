package com.konkerlabs.platform.registry.config;

import com.konkerlabs.platform.registry.business.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;

@Configuration
@EnableCaching
@EnableScheduling
public class MongoCacheConfig extends CachingConfigurerSupport {

    private static Logger LOG = LoggerFactory.getLogger(MongoCacheConfig.class);

    public MongoCacheConfig() {

    }


    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        Cache eventsSchemaCache = new ConcurrentMapCache("eventSchemaCache");
        Cache deviceCache = new ConcurrentMapCache("deviceCache");
        Cache apiKeyCache = new ConcurrentMapCache("apiKeyCache");
        Cache applicationDevicesCache = new ConcurrentMapCache("applicationDevicesCache");
        cacheManager.setCaches(Arrays.asList(eventsSchemaCache,
                deviceCache,
                apiKeyCache,
                applicationDevicesCache));
        return cacheManager;
    }

    @Scheduled(fixedRate = 1800000)
    public void evictAllCaches() {
        LOG.info("Evict every cache");
        CacheManager cacheManager = cacheManager();
        cacheManager.getCacheNames().stream()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    @Bean("customKeyGenerator")
    public KeyGenerator keyGenerator() {
        return new CustomKeyGenerator();
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
}
