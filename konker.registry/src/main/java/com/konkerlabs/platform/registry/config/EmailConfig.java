package com.konkerlabs.platform.registry.config;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class EmailConfig {
	
	private String host = "email-smtp.us-east-1.amazonaws.com";
	private String port = "587";
	private String protocol = "smtp";
	private String username = "username";
	private String password = "pass";
	private String baseurl = "http://localhost:8080/";
	private String sender = "no-reply@konkerlabs.com";
	private boolean enabled = false;
	
	public EmailConfig() {
		if (ConfigFactory.load().hasPath("email")) {
			Config config = ConfigFactory.load().getConfig("email");
			setHost(config.getString("host"));
			setPort(config.getString("port"));
			setProtocol(config.getString("protocol"));
			setUsername(config.getString("username"));
			setPassword(config.getString("password"));
			setBaseurl(config.getString("baseurl"));
			setSender(config.getString("sender"));
			setEnabled(config.hasPath("enabled") ? config.getBoolean("enabled") : false);
		}
	}
}
