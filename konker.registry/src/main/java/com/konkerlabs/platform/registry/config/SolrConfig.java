package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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
public class SolrConfig {

    private static final Config solrConfig = ConfigFactory.load().getConfig("solr");

    @Bean
    public SolrServer solrServer() {
        return new HttpSolrServer(solrConfig.getString("base.url"));
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
