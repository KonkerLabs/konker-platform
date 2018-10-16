package com.konkerlabs.platform.registry.data.core.integration.gateway;


import com.konkerlabs.platform.registry.data.core.integration.exceptions.IntegrationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.net.URI;
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
