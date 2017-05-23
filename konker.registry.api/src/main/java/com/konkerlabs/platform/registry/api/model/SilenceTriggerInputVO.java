package com.konkerlabs.platform.registry.api.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SilenceTriggerInputVO {

    @ApiModelProperty(value = "the number of minutes that platform will wait for a new message until raising an alert", example = "3600", position = 3)
    protected int minutes;

}
