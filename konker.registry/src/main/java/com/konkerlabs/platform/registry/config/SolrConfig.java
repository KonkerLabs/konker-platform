package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.server.SolrServerFactory;
import org.springframework.data.solr.server.support.MulticoreSolrServerFactory;

@Configuration
@Data
public class SolrConfig {

    private String baseUrl;
    
    public SolrConfig() {
    	Map<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("solr.base.url", "http://localhost:8983/solr/");
		Config defaultConf = ConfigFactory.parseMap(defaultMap);

		Config config = ConfigFactory.load().withFallback(defaultConf);
    	setBaseUrl(config.getString("solr.base.url"));
    }

    @Bean
    public SolrServer solrServer() {
        return new HttpSolrServer(getBaseUrl());
    }

    @Bean
    public SolrServerFactory solrServerFactory(SolrServer solrServer) {
        return new MulticoreSolrServerFactory(solrServer);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public SolrTemplate solrTemplate(SolrServerFactory solrServerFactory) {
        return new SolrTemplate(solrServerFactory);
    }
}
