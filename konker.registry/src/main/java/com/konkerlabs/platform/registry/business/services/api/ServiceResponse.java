package com.konkerlabs.platform.registry.business.services.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ServiceResponse {

    public enum Status { OK, ERROR }

    private Status status;
    private List<String> responseMessages;
    private Object result;
}
