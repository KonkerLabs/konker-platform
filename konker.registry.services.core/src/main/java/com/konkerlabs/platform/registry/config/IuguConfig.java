package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class IuguConfig {

	private String accountId;
	private String apiURL;
	private String apiToken;
	private boolean testMode;

	public IuguConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("iugu.accountId", "D30857E6ABF2462098298A5740C6B3A9");
		defaultMap.put("iugu.testMode", "true");
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setAccountId(config.getString("iugu.accountId"));
		setTestMode(config.getBoolean("iugu.testMode"));
	}

}
