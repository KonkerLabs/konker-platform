package com.konkerlabs.platform.registry.business.services.api;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class ServiceResponse<T> {

    public enum Status { OK, ERROR }

    private Status status;
    @Singular
    private List<String> responseMessages;
    private T result;
}
