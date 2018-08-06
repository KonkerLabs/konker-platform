package com.konkerlabs.platform.registry.business.services.api;

import java.util.Map;

public class ServiceResponseBuilder<T> {

    private ServiceResponse<T> response;

    private ServiceResponseBuilder(ServiceResponse<T> response) {
        this.response = response;
    }

    public static <T> ServiceResponseBuilder<T> ok() {
        ServiceResponse<T> response = new ServiceResponse<T>();
        response.setStatus(ServiceResponse.Status.OK);

        return new ServiceResponseBuilder<T>(response);
    }

    public static <T> ServiceResponseBuilder<T> error() {
        ServiceResponse<T> response = new ServiceResponse<T>();
        response.setStatus(ServiceResponse.Status.ERROR);

        return new ServiceResponseBuilder<T>(response);
    }

    public ServiceResponseBuilder<T> withMessage(String code, Object... parameters) {
        this.response.getResponseMessages().put(code,
            parameters == null || parameters.length == 0 ? null : parameters
        );
        return this;
    }

    public ServiceResponseBuilder<T> withMessages(Map<String, Object[]> messages) {
        this.response.getResponseMessages().putAll(messages);
        return this;
    }

    public ServiceResponseBuilder<T> withResult(T result) {
        this.response.setResult(result);
        return this;
    }

    public ServiceResponse<T> build() {
        return this.response;
    }
}