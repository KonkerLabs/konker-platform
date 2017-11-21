package com.konkerlabs.platform.registry.api.exceptions;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.model.OauthClientDetails;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class BadRequestResponseException extends Exception {

    private static final long serialVersionUID = -854909746416282903L;

    private boolean validationsError = false;

    private Map<String, Object[]> responseMessages;

    private String message;

    private Locale locale;

    public BadRequestResponseException(OauthClientDetails client, ServiceResponse<?> serviceResponse, Set<String> validationsCode) {

        if (serviceResponse != null &&
                serviceResponse.getResponseMessages() != null) {
            for (String key : serviceResponse.getResponseMessages().keySet()) {
                if (validationsCode.contains(key)) {
                    validationsError = true;
                    break;
                }
            }
        }

        this.responseMessages = serviceResponse != null ? serviceResponse.getResponseMessages() : null;
        this.locale = client.getLanguage().getLocale();

    }

    public BadRequestResponseException(String message) {
        this.message = message;
    }

    public boolean hasValidationsError() {
        return validationsError;
    }

    public Map<String, Object[]> getResponseMessages() {
        return responseMessages;
    }

    public String getMessage() {
        return message;
    }

    public Locale getLocale() {
        return locale;
    }

}
