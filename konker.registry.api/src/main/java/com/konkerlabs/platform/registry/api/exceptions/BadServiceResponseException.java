package com.konkerlabs.platform.registry.api.exceptions;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

public class BadServiceResponseException extends Exception {

    private static final long serialVersionUID = -854909746416282903L;

    private boolean validationsError = false;

    private Map<String, Object[]> responseMessages;

    private Locale locale;

    public BadServiceResponseException(User user, ServiceResponse<?> serviceResponse, Set<String> validationsCode) {
        this(user, serviceResponse != null ? serviceResponse.getResponseMessages() : null, validationsCode);
    }

    public BadServiceResponseException(User user, Map<String, Object[]> responseMessages, Set<String> validationsCode) {

        if (responseMessages != null && validationsCode != null) {
            for (String key : responseMessages.keySet()) {
                if (validationsCode.contains(key)) {
                    validationsError = true;
                    break;
                }
            }
        }

        this.responseMessages = responseMessages;
        this.locale = user.getLanguage().getLocale();

    }

    public boolean hasValidationsError() {
        return validationsError;
    }

    public Map<String, Object[]> getResponseMessages() {
        return responseMessages;
    }

    public Locale getLocale() {
        return locale;
    }

}
