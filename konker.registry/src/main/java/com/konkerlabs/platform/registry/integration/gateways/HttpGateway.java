package com.konkerlabs.platform.registry.integration.gateways;


import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.function.Supplier;


public interface HttpGateway {

    <T> String request(HttpMethod method,
                   URI uri,
                   Supplier<T> body,
                   String user,
                   String password) throws IntegrationException;

}
