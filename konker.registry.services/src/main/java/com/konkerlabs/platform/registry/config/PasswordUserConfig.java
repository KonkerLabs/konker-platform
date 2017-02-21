package com.konkerlabs.platform.registry.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class PasswordUserConfig {
	
	private String hashAlgorithm;
	private Integer saltSize;
	private Integer iterations;
	
	public PasswordUserConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("password.user.hash.algorithm", "SHA256");
		defaultMap.put("password.user.salt.size", 16);
		defaultMap.put("password.user.iterations", 10000);
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setHashAlgorithm(config.getString("password.user.hash.algorithm"));
		setSaltSize(config.getInt("password.user.salt.size"));
		setIterations(config.getInt("password.user.iterations"));
	}

}
