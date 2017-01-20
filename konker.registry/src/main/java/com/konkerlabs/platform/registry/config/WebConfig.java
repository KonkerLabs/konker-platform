package com.konkerlabs.platform.registry.config;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class WebConfig {

	private boolean viewsCache = false;
	
	public WebConfig() {
		if (ConfigFactory.load().hasPath("web")) {
			setViewsCache(ConfigFactory.load().getConfig("web").getBoolean("views.cache"));
		}
	}
	
}
