package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class KonkerInvoiceApiConfig {

	private String url;
	private String username;
	private String password;

	public KonkerInvoiceApiConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("konkerInvoiceApi.url", "http://localhost:8000");
		defaultMap.put("konkerInvoiceApi.username", "konker-console");
		defaultMap.put("konkerInvoiceApi.password", "goKonk4rGo");
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setUrl(config.getString("konkerInvoiceApi.url"));
		setUsername(config.getString("konkerInvoiceApi.username"));
		setPassword(config.getString("konkerInvoiceApi.password"));
	}

}
