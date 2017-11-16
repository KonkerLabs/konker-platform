package com.konkerlabs.platform.registry.api.exceptions;

import com.konkerlabs.platform.registry.business.model.OauthClientDetails;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NotAuthorizedResponseException extends Exception {

    private static final long serialVersionUID = -854909746416282903L;

    private boolean validationsError = false;

    private Map<String, Object[]> responseMessages;

    private Locale locale;

    public NotAuthorizedResponseException(OauthClientDetails user, ServiceResponse<?> serviceResponse, Set<String> validationsCode) {

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
