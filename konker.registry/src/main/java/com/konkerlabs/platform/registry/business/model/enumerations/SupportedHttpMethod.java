package com.konkerlabs.platform.registry.business.model.enumerations;

import org.springframework.http.HttpMethod;

public enum SupportedHttpMethod {

    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE");

    private String code;

    SupportedHttpMethod(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public HttpMethod getHttpMethod(){
        return HttpMethod.resolve(code);
    }

}
