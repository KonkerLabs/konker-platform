package com.konkerlabs.platform.registry.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.typesafe.config.ConfigFactory;

@Configuration
public class SmsConfig {

	@Bean(name = "enableSms")
	public boolean enableSms() {
		return ConfigFactory.load().getConfig("sms").getBoolean("enable");
	}

}