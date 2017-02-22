package com.konkerlabs.platform.registry.api.model;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RestResponseBuilder<T> {

    private RestResponse<T> response;

    private HttpStatus httpStatus;

    private RestResponseBuilder(RestResponse<T> response) {
        this.response = response;
    }

    public static <T> RestResponseBuilder<T> ok() {
        RestResponse<T> response = new RestResponse<T>();
        response.setStatus(RestResponse.Status.OK);
        response.setTimestamp(Instant.now().getEpochSecond());
        
        return new RestResponseBuilder<T>(response).withHttpStatus(HttpStatus.OK);
    };

    public static <T> RestResponseBuilder<T> error() {
        RestResponse<T> response = new RestResponse<T>();
        response.setStatus(RestResponse.Status.ERROR);
        response.setTimestamp(Instant.now().getEpochSecond());
        
        return new RestResponseBuilder<T>(response).withHttpStatus(HttpStatus.BAD_REQUEST);
    };

    public RestResponseBuilder<T> withHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    
    public RestResponseBuilder<T> withMessage(String code, Object... parameters) {
        this.response.getResponseMessages().put(code, parameters == null || parameters.length == 0 ? null : parameters);
        return this;
    }

    public RestResponseBuilder<T> withMessages(Map<String, Object[]> messages) {
        this.response.getResponseMessages().putAll(messages);
        return this;
    }

    public RestResponseBuilder<T> withResult(T result) {
        this.response.setResult(result);
        return this;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ResponseEntity build() {
        this.response.setHttpStatus(httpStatus.value());
        return new ResponseEntity(this.response, httpStatus);
    }

}