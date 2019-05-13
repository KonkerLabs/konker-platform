package com.konkerlabs.platform.registry.api.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@EnableAutoConfiguration
@Data
public class PubServerInternalConfig {

	private String url;
	
	public PubServerInternalConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("pubServerInternal.url", "http://localhost:8085/registry-data-processor/{0}/{1}/pub");
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setUrl(config.getString("pubServerInternal.url"));
	}
}
