package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class UTraceConfig {

	private String id;
	private boolean enable;

	public UTraceConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("uTrace.id", "ID");
		defaultMap.put("uTrace.enable", false);
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setId(config.getString("uTrace.id"));
		setEnable(config.getBoolean("uTrace.enable"));
	}

}
