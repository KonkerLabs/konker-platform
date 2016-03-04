package com.konkerlabs.platform.registry.integration.gateways;


import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;

import java.net.URI;


public interface HttpEnrichmentGateway {

    String get(URI uri, String user, String password) throws IntegrationException;
}
