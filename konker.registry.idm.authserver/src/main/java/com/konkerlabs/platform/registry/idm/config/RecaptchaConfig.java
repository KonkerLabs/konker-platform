package com.konkerlabs.platform.registry.idm.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class RecaptchaConfig {

	private String secretKey;
	private String siteKey;
	private String host;
	private boolean enabled;
	
	public RecaptchaConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("recaptcha.secretKey", "your-key");
		defaultMap.put("recaptcha.siteKey", "site-key");
		defaultMap.put("recaptcha.host", "localhost");
		defaultMap.put("recaptcha.enabled", false);
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);

		if (config.getBoolean("recaptcha.enabled")) {
			setSecretKey(config.getString("recaptcha.secretKey"));
			setSiteKey(config.getString("recaptcha.siteKey"));
			setHost(config.getString("recaptcha.host"));
			setEnabled(config.getBoolean("recaptcha.enabled"));
		}
	}
			  
}
