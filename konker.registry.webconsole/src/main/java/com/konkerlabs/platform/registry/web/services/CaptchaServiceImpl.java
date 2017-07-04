package com.konkerlabs.platform.registry.web.services;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.web.services.api.CaptchaService;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;

/**
 * Created by Felipe on 03/01/17.
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Autowired
    private JsonParsingService jsonParsingService;

    @Autowired
    private CaptchaRestClient restClient;

    private Logger LOG = LoggerFactory.getLogger(CaptchaServiceImpl.class);

    public CaptchaServiceImpl() {
    }

    @Override
    public ServiceResponse<Boolean> validateCaptcha(String secret, String response, String host) {
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        String url = "https://www.google.com/recaptcha/web/siteverify";
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
            body = restClient.request(
                    finalUrl.toURI());
            result = jsonParsingService.toMap(body);
        } catch (Exception e) {
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