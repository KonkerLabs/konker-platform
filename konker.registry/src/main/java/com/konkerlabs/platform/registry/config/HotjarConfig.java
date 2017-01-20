package com.konkerlabs.platform.registry.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class HotjarConfig {
	
	private Integer id;
	private boolean enable;
	
	public HotjarConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("hotjar.id", 0);
		defaultMap.put("hotjar.enable", true);
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setId(config.getInt("hotjar.id"));
		setEnable(config.getBoolean("hotjar.enable"));
	}

}
