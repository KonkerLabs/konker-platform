package com.konkerlabs.platform.registry.web.controllers;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

@Controller
@Scope("session")
public class DashboardsController {

    private static final Config solrConfig = ConfigFactory.load().getConfig("solr");
    private static final String solrBaseUrl = solrConfig.getString("base.url");

    @Autowired
    private Tenant tenant;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
    }

    @RequestMapping("solr/**")
    public ResponseEntity<String> requestOnSolr(RequestEntity<String> requestEntity,
                                                final HttpServletRequest httpServletRequest) throws URISyntaxException, UnsupportedEncodingException {
        HttpEntity httpEntity = new HttpEntity(requestEntity.getBody(),requestEntity.getHeaders());
        URI uri = buildURI(solrBaseUrl,
                httpServletRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
                        .toString().replaceAll("\\/solr\\/",""),
                httpServletRequest.getQueryString());
        return restTemplate.exchange(uri, requestEntity.getMethod(), httpEntity, String.class);
    }

    private URI buildURI(String solrHost, String path, String query) {
        return URI.create(solrHost +
                path +
                (query != null && !query.isEmpty() ? "?" + query : ""));
    }
}
