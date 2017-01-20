package com.konkerlabs.platform.registry.config;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class HotjarConfig {
	
	private Integer id = 000000;
	private boolean enable = true;
	
	public HotjarConfig() {
		if (ConfigFactory.load().hasPath("hotjar")) {
			Config config = ConfigFactory.load().getConfig("hotjar");
			setId(config.getInt("id"));
			setEnable(config.getBoolean("enable"));
		}
	}

}
