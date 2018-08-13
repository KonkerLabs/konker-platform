package com.konkerlabs.platform.registry.data.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class S3BucketConfig {

	private String name;
	private String prefix;
	private String key;
	private String secret;
	private Integer maxSize;
	private String fileTypes;
	private boolean enabled;
	
	public S3BucketConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("s3bucket.name", "user");
		defaultMap.put("s3bucket.prefix", "https://s3.eu-central-1.amazonaws.com");
		defaultMap.put("s3bucket.key", "KEY");
		defaultMap.put("s3bucket.secret", "PASS");
		defaultMap.put("s3bucket.max-size", 500000);
		defaultMap.put("s3bucket.file-types", "jpg,png,jpeg");
		defaultMap.put("s3bucket.enabled", false);
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setName(config.getString("s3bucket.name"));
		setPrefix(config.getString("s3bucket.prefix"));
		setKey(config.getString("s3bucket.key"));
		setSecret(config.getString("s3bucket.secret"));
		setMaxSize(config.getInt("s3bucket.max-size"));
		setFileTypes(config.getString("s3bucket.file-types"));
		setEnabled(config.getBoolean("s3bucket.enabled"));
	}

}
