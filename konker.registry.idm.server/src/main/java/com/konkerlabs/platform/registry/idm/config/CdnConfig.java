package com.konkerlabs.platform.registry.idm.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class CdnConfig {

	private String name;
	private String prefix;
	private String key;
	private String secret;
	private Integer maxSize;
	private String fileTypes;
	private boolean enabled;
	
	public CdnConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("cdn.name", "user");
		defaultMap.put("cdn.prefix", "https://s3.eu-central-1.amazonaws.com");
		defaultMap.put("cdn.key", "KEY");
		defaultMap.put("cdn.secret", "PASS");
		defaultMap.put("cdn.max-size", 500000);
		defaultMap.put("cdn.file-types", "jpg,png,jpeg");
		defaultMap.put("cdn.enabled", false);
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setName(config.getString("cdn.name"));
		setPrefix(config.getString("cdn.prefix"));
		setKey(config.getString("cdn.key"));
		setSecret(config.getString("cdn.secret"));
		setMaxSize(config.getInt("cdn.max-size"));
		setFileTypes(config.getString("cdn.file-types"));
		setEnabled(config.getBoolean("cdn.enabled"));
	}

}
