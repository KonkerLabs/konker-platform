package com.konkerlabs.platform.registry.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class SmsConfig {
	
	private String uri;
	private String username;
	private String password;
	private String from;
	private boolean enabled;
	
	public SmsConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("sms.uri", "http://api.com/endpoint");
		defaultMap.put("sms.username", "user");
		defaultMap.put("sms.password", "pass");
		defaultMap.put("sms.from", "+99999999999");
		defaultMap.put("sms.enabled", false);
		Config defaultConf = ConfigFactory.parseMap(defaultMap);
		
		Config config = ConfigFactory.load().withFallback(defaultConf);
		setUri(config.getString("sms.uri"));
		setUsername(config.getString("sms.username"));
		setPassword(config.getString("sms.password"));
		setFrom(config.getString("sms.from"));
		setEnabled(config.getBoolean("sms.enabled"));
	}
}
