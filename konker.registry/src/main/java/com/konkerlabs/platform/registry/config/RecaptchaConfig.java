package com.konkerlabs.platform.registry.config;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class RecaptchaConfig {

	private String secretKey = "your-key";
	private String siteKey = "site-key";
	private String host = "localhost";
	
	public RecaptchaConfig() {
		if (ConfigFactory.load().hasPath("recaptcha")) {
			Config config = ConfigFactory.load().getConfig("recaptcha");
			setSecretKey(config.getString("secretKey"));
			setSiteKey(config.getString("siteKey"));
			setHost(config.getString("host"));
		}
	}
			  
}
