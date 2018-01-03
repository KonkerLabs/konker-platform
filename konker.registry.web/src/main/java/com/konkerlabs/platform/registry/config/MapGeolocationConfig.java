package com.konkerlabs.platform.registry.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class MapGeolocationConfig {

	private String apiKey;
	private boolean enabled;

	public MapGeolocationConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("map.apiKey", "");
		defaultMap.put("map.enabled", false);
		Config defaultConf = ConfigFactory.parseMap(defaultMap);
		
		Config config = ConfigFactory.load().withFallback(defaultConf);
		setApiKey(config.getString("map.apiKey"));
		setEnabled(config.getBoolean("map.enabled"));
	}

}
