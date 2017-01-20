package com.konkerlabs.platform.registry.config;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class IntegrationConfig {

	private Integer timeoutDefault = 30000;
	private Integer enrichment = 30000;
	private Integer sms = 30000;
	                
	public IntegrationConfig() {
		if (ConfigFactory.load().hasPath("integration")) {
			Config config = ConfigFactory.load().getConfig("integration");
			setTimeoutDefault(Integer.parseInt(config.getObjectList("timeout").get(0).get("default").render()));
			setEnrichment(Integer.parseInt(config.getObjectList("timeout").get(0).get("enrichment").render()));
			setSms(Integer.parseInt(config.getObjectList("timeout").get(0).get("sms").render()));
		}
	}
}
