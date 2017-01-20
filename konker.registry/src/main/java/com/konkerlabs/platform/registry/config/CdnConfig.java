package com.konkerlabs.platform.registry.config;

import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class CdnConfig {

	private String name = "user";
	private String prefix = "https://s3.eu-central-1.amazonaws.com";
	private String key = "KEY";
	private String secret = "PASS";
	private Integer maxSize =500000;
	private String fileTypes = "jpg,png,jpeg";
	private boolean enabled = false;
	
	public CdnConfig() {
		if (ConfigFactory.load().hasPath("cdn")) {
			Config config = ConfigFactory.load().getConfig("cdn");
			setName(config.getString("name"));
			setPrefix(config.getString("prefix"));
			setKey(config.getString("key"));
			setSecret(config.getString("secret"));
			setMaxSize(config.getInt("max-size"));
			setFileTypes(config.getString("file-types"));
			setEnabled(config.getBoolean("enabled"));
		} 
	}

}
