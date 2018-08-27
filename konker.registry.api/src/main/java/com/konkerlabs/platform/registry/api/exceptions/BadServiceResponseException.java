package com.konkerlabs.platform.registry.api.exceptions;

import java.util.Map;
import java.util.Set;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

public class BadServiceResponseException extends Exception {

    private static final long serialVersionUID = -854909746416282903L;

    private boolean validationsError = false;

    private Map<String, Object[]> responseMessages;

    public BadServiceResponseException(ServiceResponse<?> serviceResponse, Set<String> validationsCode) {
        this(serviceResponse != null ? serviceResponse.getResponseMessages() : null, validationsCode);
    }

    public BadServiceResponseException(Map<String, Object[]> responseMessages, Set<String> validationsCode) {

        if (responseMessages != null && validationsCode != null) {
            for (String key : responseMessages.keySet()) {
                if (validationsCode.contains(key)) {
                    validationsError = true;
                    break;
                }
            }
        }

        this.responseMessages = responseMessages;
    }

    public boolean hasValidationsError() {
        return validationsError;
    }

    public Map<String, Object[]> getResponseMessages() {
        return responseMessages;
    }

}
