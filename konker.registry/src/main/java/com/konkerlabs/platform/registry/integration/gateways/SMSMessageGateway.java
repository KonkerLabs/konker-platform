package com.konkerlabs.platform.registry.integration.gateways;

import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;

public interface SMSMessageGateway {
    void send(String text, String phoneNumber) throws IntegrationException;
}
