package com.konkerlabs.platform.registry.config;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class SmsConfig {
	
	private String uri = "http://api.com/endpoint";
	private String username = "user";
	private String password = "pass";
	private String from = "+99999999999";
	private boolean enabled = false;
	
	public SmsConfig() {
		if (ConfigFactory.load().hasPath("sms")) {
			Config config = ConfigFactory.load().getConfig("sms");
			setUri(config.getString("uri"));
			setUsername(config.getString("username"));
			setPassword(config.getString("password"));
			setFrom(config.getString("from"));
			setEnabled(config.getBoolean("enabled"));
		}
	}
}
