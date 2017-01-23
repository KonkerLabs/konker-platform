package com.konkerlabs.platform.registry.integration.gateways;

import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import static java.text.MessageFormat.format;

@Component
public class HttpGatewayImpl implements HttpGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpGatewayImpl.class);
    private static Config config = ConfigFactory.load().getConfig("integration");

    @Autowired
    private RestTemplate restTemplate;

    private HttpHeaders defaultHeaders;

    @Override
    public <T> String request(
            HttpMethod method,
            HttpHeaders headers,
            URI uri,
            MediaType mediaType,
            Supplier<T> body,
            String username,
            String password) throws IntegrationException {

        Optional.ofNullable(method)
                .orElseThrow(() -> new IllegalStateException("HTTP method must be provided"));

        Optional.ofNullable(uri)
                .orElseThrow(() -> new IllegalStateException("Service URI must be provided"));

        Optional.ofNullable(restTemplate)
                .orElseThrow(() -> new IllegalStateException("RestTemplate must be provided"));

        Optional.ofNullable(headers).orElse(new HttpHeaders());

        if ((username != null && password == null) || username == null && password != null) {
            throw new IllegalStateException("Username and Password must be both provided together");
        }

        try {

            Optional.ofNullable(defaultHeaders).ifPresent(item -> {
                item.entrySet().forEach(header->{
                    headers.put(header.getKey(), header.getValue());
                });
            });

            if ((username != null && !username.trim().isEmpty()) || (password != null && !password.trim().isEmpty())) {
                String encodedCredentials = Base64Utils
                        .encodeToString(format("{0}:{1}", username, password).getBytes());

                headers.add("Authorization", format("Basic {0}", encodedCredentials));
            }

            HttpEntity<String> entity = new HttpEntity(
                    Optional.ofNullable(body).orElse(() -> null).get(),
                    headers
            );

            LOGGER.debug("Requesting {} from {}.", method, uri);
            setUp(null, mediaType);
            ResponseEntity<String> exchange = restTemplate.exchange(uri, method, entity, String.class);

            if (exchange.getStatusCode().is2xxSuccessful()) {
                return exchange.getBody();
            } else
                throw new IntegrationException(format("Exception while requesting {0} from {1}. Status Code: {2}. Message: {3}.",
                        method,
                        uri,
                        exchange.getStatusCode(),
                        exchange.getBody()));

        } catch (RestClientException rce) {
            throw new IntegrationException(
                    format("Exception while requesting {0} from {1}", method, uri), rce);
        }
    }

    private void setUp(Optional<Integer> timeout, MediaType mediaType) {

        defaultHeaders = new HttpHeaders();
        defaultHeaders.setContentType(mediaType);
        defaultHeaders.add(KONKER_VERSION_HEADER, "0.1");

        Integer clientTimeout =
                Optional.ofNullable(timeout).isPresent() ?
                        timeout.get() :
                        Integer.parseInt(config.getObjectList("timeout").get(0).get("default").render());

        Optional.ofNullable(restTemplate.getRequestFactory()).ifPresent(item -> {
            ((SimpleClientHttpRequestFactory) item).setReadTimeout(clientTimeout);
            ((SimpleClientHttpRequestFactory) item).setConnectTimeout(clientTimeout);
        });
    }
}
