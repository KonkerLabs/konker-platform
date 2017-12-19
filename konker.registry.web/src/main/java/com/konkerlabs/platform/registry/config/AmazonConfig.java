package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class AmazonConfig {

	private boolean kinesisRouteEnabled;

	public AmazonConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("amazon.kinesisRouteEnabled", false);
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setKinesisRouteEnabled(config.getBoolean("amazon.kinesisRouteEnabled"));
	}

}
