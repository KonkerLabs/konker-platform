package com.konkerlabs.platform.registry.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Data
@Configuration
public class HealthAlertsConfig {

    private int silenceMinimumMinutes;

    public HealthAlertsConfig() {
        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put("healthAlerts.silence.minimumMinutes", 10);

        Config defaultConf = ConfigFactory.parseMap(defaultMap);
        Config config = ConfigFactory.load().withFallback(defaultConf);

        setSilenceMinimumMinutes(config.getInt("healthAlerts.silence.minimumMinutes"));
    }

}
