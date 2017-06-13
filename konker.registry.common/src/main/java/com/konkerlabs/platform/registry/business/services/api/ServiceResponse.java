package com.konkerlabs.platform.registry.business.services.api;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ServiceResponse<T> {

    public enum Status { OK, ERROR }

    private Status status;
    private Map<String, Object[]> responseMessages = new HashMap<>();
    private T result;

    public Boolean isOk() {
        return getStatus().equals(Status.OK);
    }
}
