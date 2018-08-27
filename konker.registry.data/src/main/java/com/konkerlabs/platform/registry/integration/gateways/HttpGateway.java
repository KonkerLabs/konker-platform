package com.konkerlabs.platform.registry.integration.gateways;


import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import org.apache.http.Header;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;


public interface HttpGateway {

    String KONKER_VERSION_HEADER = "X-Konker-Version";

    <T> String request(HttpMethod method,
                       HttpHeaders headers,
                       URI uri,
                       MediaType mediaType,
                       Supplier<T> body,
                       String user,
                       String password) throws IntegrationException;

}
