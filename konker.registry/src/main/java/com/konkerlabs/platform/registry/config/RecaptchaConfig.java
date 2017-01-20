package com.konkerlabs.platform.registry.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class RecaptchaConfig {

	private String secretKey;
	private String siteKey;
	private String host;
	
	public RecaptchaConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("recaptcha.secretKey", "your-key");
		defaultMap.put("recaptcha.siteKey", "site-key");
		defaultMap.put("recaptcha.host", "localhost");
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);

		setSecretKey(config.getString("recaptcha.secretKey"));
		setSiteKey(config.getString("recaptcha.siteKey"));
		setHost(config.getString("recaptcha.host"));
	}
			  
}
