package com.konkerlabs.platform.registry.test.base;

import com.konkerlabs.platform.registry.config.SolrConfig;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.server.SolrServerFactory;

@Configuration
public class SolrTestConfiguration extends SolrConfig {

    @Bean
    public SolrTemplate solrTemplate(SolrServerFactory solrServerFactory) {
        return Mockito.mock(SolrTemplate.class);
    }
}
