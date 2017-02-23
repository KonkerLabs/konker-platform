package com.konkerlabs.platform.registry.api.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_EMPTY)
public class RestResponse<T> {

    public enum Status { OK, ERROR }

    private Long timestamp;
    private int httpStatus;
    private Status status;
    private List<String> responseMessages = new ArrayList<>();
    private T result;

}
