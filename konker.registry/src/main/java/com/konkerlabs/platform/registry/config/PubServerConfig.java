package com.konkerlabs.platform.registry.config;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class PubServerConfig {

	private String httpHostname = "dev-server";
	private String httpPort = "8080";
	private String httpCtx = "registry";
	private String httpsPort = "443";
	private String mqttHostName = "dev-server";
	private String mqttPort = "1883";
	private String mqttTlsPort = "1883";
	
	public PubServerConfig() {
		if (ConfigFactory.load().hasPath("pubServer")) {
			Config config = ConfigFactory.load().getConfig("pubServer");
			setHttpHostname(config.getString("httpHostname"));
			setHttpPort(config.getString("httpPort"));
			setHttpCtx(config.getString("httpCtx"));
			setHttpsPort(config.getString("httpsPort"));
			setMqttHostName(config.getString("mqttHostName"));
			setMqttPort(config.getString("mqttPort"));
			setMqttTlsPort(config.getString("mqttTlsPort"));
		}
	}

}
