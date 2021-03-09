package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class GoogleAnalyticsConfig {

	private String id;
	private boolean enabled;

	public GoogleAnalyticsConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("googleAnalytics.id", "ID");
		defaultMap.put("googleAnalytics.enabled", false);
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setId(config.getString("googleAnalytics.id"));
		setEnabled(config.getBoolean("googleAnalytics.enabled"));
	}

}
