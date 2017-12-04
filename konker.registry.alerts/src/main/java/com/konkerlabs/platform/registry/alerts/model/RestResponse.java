package com.konkerlabs.platform.registry.alerts.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(Include.NON_EMPTY)
@ApiModel(
        value = "RestResponse",
        discriminator = "com.konkerlabs.platform.registry.alerts.model",
        subTypes = DeviceVO.class
)
public class RestResponse<T> {

    public enum Status { SUCCESS, ERROR }

    @ApiModelProperty(value = "the timestamp from response")
    private Long timestamp;
    @ApiModelProperty(value = "the http status code from response")
    private int code;
    @ApiModelProperty(value = "the status from response", allowableValues = "SUCCESS, ERROR")
    private String status;
    @ApiModelProperty(value = "the error messages")
    private List<String> messages = new ArrayList<>();
    @JsonInclude(Include.ALWAYS)
    @ApiModelProperty(value = "the response")
    private T result;

    public void setStatus(Status status) {
        this.status = status.name().toLowerCase();
    }

}
