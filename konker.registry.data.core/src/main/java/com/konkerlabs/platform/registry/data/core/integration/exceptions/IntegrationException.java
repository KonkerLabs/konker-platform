package com.konkerlabs.platform.registry.data.core.integration.exceptions;

public class IntegrationException extends Exception {
    private static final long serialVersionUID = -8633271049261013566L;

    public IntegrationException(String message) {
        super(message);
    }

    public IntegrationException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

}
