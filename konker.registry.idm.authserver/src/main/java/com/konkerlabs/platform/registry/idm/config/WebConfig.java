package com.konkerlabs.platform.registry.idm.config;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class WebConfig {

    private boolean viewsCache = false;

    public WebConfig() {
        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put("web.views.cache", false);
        Config defaultConf = ConfigFactory.parseMap(defaultMap);

        Config config = ConfigFactory.load().withFallback(defaultConf);
        setViewsCache(config.getBoolean("web.views.cache"));
    }

}