package com.konkerlabs.platform.registry.integration.gateways;


import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.net.URI;
import java.util.function.Supplier;


public interface HttpGateway {

    <T> String request(HttpMethod method,
                       URI uri,
                       MediaType mediaType,
                       Supplier<T> body,
                       String user,
                       String password) throws IntegrationException;

}
