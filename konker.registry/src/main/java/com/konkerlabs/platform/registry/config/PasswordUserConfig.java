package com.konkerlabs.platform.registry.config;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class PasswordUserConfig {
	
	private String hashAlgorithm = "SHA256";
	private Integer saltSize = 16;
	private Integer iterations = 10000;
	
	public PasswordUserConfig() {
		if (ConfigFactory.load().hasPath("password.user")) {
			Config config = ConfigFactory.load().getConfig("password.user");
			setHashAlgorithm(config.getString("hash.algorithm"));
			setSaltSize(config.getInt("salt.size"));
			setIterations(config.getInt("iterations"));
		}
	}

}
