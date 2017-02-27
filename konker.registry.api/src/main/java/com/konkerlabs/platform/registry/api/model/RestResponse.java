package com.konkerlabs.platform.registry.api.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonInclude(Include.NON_EMPTY)
@ApiModel(
        value = "RestResponse",
        discriminator = "com.konkerlabs.platform.registry.api.model",
        subTypes = DeviceVO.class
)
public class RestResponse<T> {

    public enum Status { OK, ERROR }


    @ApiModelProperty(value = "the timestamp from response")
    private Long timestamp;
    @ApiModelProperty(value = "the httpstatus from response")
    private int httpStatus;
    @ApiModelProperty(value = "the status from response", allowableValues = "OK,ERROR")
    private Status status;
    @ApiModelProperty(value = "the error messages")
    private List<String> responseMessages = new ArrayList<>();
    @ApiModelProperty(value = "the entity")
    private T result;

}
