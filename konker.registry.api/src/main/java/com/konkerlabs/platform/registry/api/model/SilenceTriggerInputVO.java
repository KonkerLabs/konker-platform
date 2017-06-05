package com.konkerlabs.platform.registry.api.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SilenceTriggerInputVO {

    @ApiModelProperty(value = "the number of minutes that platform will wait for a new message until raising an alert", example = "2000", position = 10)
    protected int minutes;
    @ApiModelProperty(value = "a brief description of the trigger", example = "inactivity alert", position = 1)
    protected String description;

}
