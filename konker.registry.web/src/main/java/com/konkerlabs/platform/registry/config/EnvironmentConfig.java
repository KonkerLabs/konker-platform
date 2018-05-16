package com.konkerlabs.platform.registry.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class EnvironmentConfig {

	private int csvDownloadRowsLimit;

	public EnvironmentConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("environment.preferences.csvDownloadRowsLimit", 50000);
		Config defaultConf = ConfigFactory.parseMap(defaultMap);
		
		Config config = ConfigFactory.load().withFallback(defaultConf);
		setCsvDownloadRowsLimit(config.getInt("environment.preferences.csvDownloadRowsLimit"));
	}

}
