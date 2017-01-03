package com.konkerlabs.platform.registry.business.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konkerlabs.platform.registry.business.services.api.CaptchaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Felipe on 03/01/17.
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    public CaptchaServiceImpl(){
    }

    @Override
    public ServiceResponse<Map<String, Object>> validateCaptcha(String secret, String response, String host) {
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        String url = "https://www.google.com/recaptcha/api/siteverify";
        String query = null;
        try {
            query = String.format("secret=%s&response=%s&remoteip=%s",
                    URLEncoder.encode(secret, charset),
                    URLEncoder.encode(response, charset),
                    URLEncoder.encode(host, charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url + "?" + query);

        HttpResponse httpResponse;
        try {
            httpResponse = client.execute(post);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(httpResponse.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }

            return ServiceResponseBuilder.<Map<String, Object>>ok().withResult(
                    new ObjectMapper().readValue(result.toString(), HashMap.class)
            ).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ServiceResponseBuilder.<Map<String, Object>>error()
                .withResult(Collections.emptyMap()).build();
    }
}