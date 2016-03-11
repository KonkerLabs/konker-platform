package com.konkerlabs.platform.registry.integration.gateways;

import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HttpEnrichmentGatewayImpl implements HttpEnrichmentGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEnrichmentGatewayImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String get(URI uri, String username, String password) throws IntegrationException {
        Optional.ofNullable(uri).orElseThrow(() -> new IllegalStateException("Service URI must be provided"));

        Optional.ofNullable(restTemplate)
                .orElseThrow(() -> new IllegalStateException("RestTemplate must be provided"));

        if ((username != null && password == null) || username == null && password != null) {
            throw new IllegalStateException("Username and Password must be both provided together");
        }

        try {
            HttpHeaders headers = new HttpHeaders();;
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            if (username != null && password != null) {
                String encodedCredentials = Base64Utils
                        .encodeToString(MessageFormat.format("{0}:{1}", username, password).getBytes());

                headers.add("Authorization", MessageFormat.format("Basic {0}", encodedCredentials));
            }

            HttpEntity<String> entity = new HttpEntity<String>(headers);

            LOGGER.debug("Requesting GET from {}.", uri);


            ResponseEntity<String> exchange = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

            Optional.ofNullable(exchange).orElseThrow(() -> new IntegrationException(MessageFormat.format("Exception while requesting GET from {0}.", uri)));

            if (exchange.getStatusCode().equals(HttpStatus.OK)) {
                return exchange.getBody();
            } else
                throw new IntegrationException(MessageFormat.format("Exception while requesting GET from {0}. Status Code: {1}. Message: {2}.",
                        uri,
                        exchange.getStatusCode(),
                        exchange.getBody()));

        } catch (RestClientException rce) {
            throw new IntegrationException(
                    MessageFormat.format("Exception while requesting GET from {0}", uri), rce);
        }
    }
}
