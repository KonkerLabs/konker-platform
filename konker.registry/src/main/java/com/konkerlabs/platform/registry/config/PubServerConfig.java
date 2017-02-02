package com.konkerlabs.platform.registry.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class PubServerConfig {

	private String httpHostname;
	private String httpPort;
	private String httpCtx;
	private String httpsPort;
	private String mqttHostName;
	private String mqttPort;
	private String mqttTlsPort;
	private boolean sslEnabled;
	
	public PubServerConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("pubServer.httpHostname", "dev-server");
		defaultMap.put("pubServer.httpPort", "8080");
		defaultMap.put("pubServer.httpCtx", "registry");
		defaultMap.put("pubServer.httpsPort", "443");
		defaultMap.put("pubServer.mqttHostName", "dev-server");
		defaultMap.put("pubServer.mqttPort", "1883");
		defaultMap.put("pubServer.mqttTlsPort", "1883");
		defaultMap.put("pubServer.sslEnabled", false);
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setHttpHostname(config.getString("pubServer.httpHostname"));
		setHttpPort(config.getString("pubServer.httpPort"));
		setHttpCtx(config.getString("pubServer.httpCtx"));
		setHttpsPort(config.getString("pubServer.httpsPort"));
		setMqttHostName(config.getString("pubServer.mqttHostName"));
		setMqttPort(config.getString("pubServer.mqttPort"));
		setMqttTlsPort(config.getString("pubServer.mqttTlsPort"));
		setSslEnabled(config.getBoolean("pubServer.sslEnabled"));
	}

}
