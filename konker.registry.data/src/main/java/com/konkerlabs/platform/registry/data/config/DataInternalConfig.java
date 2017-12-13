package com.konkerlabs.platform.registry.data.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Configuration
@Data
public class DataInternalConfig {

	private Integer timeoutDefault;
	private Integer enrichment;

	public DataInternalConfig() {
		Map<String, Object> defaultMap = new HashMap<>();
		Map<String, Integer> valuesTimeout = new HashMap<>();
		valuesTimeout.put("default", 30000);
		valuesTimeout.put("enrichment", 30000);
		defaultMap.put("integration.timeout", Collections.singleton(valuesTimeout));
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config defaultConfListValue = ConfigFactory.parseMap(valuesTimeout);

		Config config = ConfigFactory.load().withFallback(defaultConf);
		setTimeoutDefault(Integer.parseInt(config.getObjectList("integration.timeout").get(0).withFallback(defaultConfListValue).get("default").render()));
		setEnrichment(Integer.parseInt(config.getObjectList("integration.timeout").get(0).withFallback(defaultConfListValue).get("enrichment").render()));
	}

    @Bean
    public RestTemplate enrichmentRestTemplate() {
        return new RestTemplate();
    }

}
