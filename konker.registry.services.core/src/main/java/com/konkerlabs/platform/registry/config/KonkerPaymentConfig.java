package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class KonkerPaymentConfig {

	private String url;
	private String apiToken;

	public KonkerPaymentConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("konkerPayment.url", "http://localhost:80");
		defaultMap.put("konkerPayment.apiToken", "b17421313f9a8db907afa7b7047fbcd8");
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setUrl(config.getString("konkerPayment.url"));
		setApiToken(config.getString("konkerPayment.apiToken"));
	}

}
