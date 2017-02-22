package com.konkerlabs.platform.registry.api.model;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;;

@Data
@JsonInclude(Include.NON_EMPTY)
public class RestResponse<T> {

    public enum Status { OK, ERROR }

    private Long timestamp;
    private int httpStatus;
    private Status status;
    private Map<String, Object[]> responseMessages = new HashMap<>();
    private T result;

}
