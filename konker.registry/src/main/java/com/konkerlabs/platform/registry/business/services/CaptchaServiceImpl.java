package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.services.api.CaptchaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.json.JSONObject;
import org.json.JSONString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by Felipe on 03/01/17.
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Autowired
    private HttpGateway httpGateway;

    @Autowired
    private JsonParsingService jsonParsingService;

    private Logger LOG = LoggerFactory.getLogger(CaptchaServiceImpl.class);

    public CaptchaServiceImpl() {
    }

    @Override
    public ServiceResponse<Boolean> validateCaptcha(String secret, String response, String host) {
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        String url = "https://www.google.com/recaptcha/api/siteverify";
        String query;
        URL finalUrl;

        try {
            query = String.format("secret=%s&response=%s&remoteip=%s",
                    URLEncoder.encode(secret, charset),
                    URLEncoder.encode(response, charset),
                    URLEncoder.encode(host, charset));
            finalUrl = new URL(url + "?" + query);
        } catch (UnsupportedEncodingException | MalformedURLException | NullPointerException e) {
            LOG.error("Encoding captcha parameters error", e);
            return ServiceResponseBuilder.<Boolean>error().withResult(Boolean.FALSE).build();
        }

        String body;
        Map<String, Object> result;
        try {
            body = httpGateway.request(
                    HttpMethod.POST,
                    new HttpHeaders(),
                    finalUrl.toURI(), MediaType.APPLICATION_JSON, null, null, null);
            result = jsonParsingService.toMap(body);
        } catch (URISyntaxException | IOException | IntegrationException | IllegalArgumentException e) {
            LOG.error("Captcha processing error", e);
            return ServiceResponseBuilder.<Boolean>error().withResult(Boolean.FALSE).build();
        }
        if ((Boolean.parseBoolean(result.get("success").toString()))) {
            return ServiceResponseBuilder.<Boolean>ok().withResult(Boolean.TRUE).build();
        } else {
            return ServiceResponseBuilder.<Boolean>error().withResult(Boolean.FALSE).build();
        }
    }
}