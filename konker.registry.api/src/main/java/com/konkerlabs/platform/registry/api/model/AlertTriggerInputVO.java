package com.konkerlabs.platform.registry.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
        value = "AlertTriggerInput",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class AlertTriggerInputVO {

    @ApiModelProperty(value = "the trigger name", example = "silence trigger", position = 1)
    protected String name;
    @ApiModelProperty(value = "alert type", example = "silence", position = 2)
    protected String type;
    @ApiModelProperty(value = "a brief description of the trigger", example = "inactivity alert", position = 3)
    protected String description;
    @ApiModelProperty(value = "the device model name of device", example = "PresenceSensor", position = 4)
    protected String deviceModelName;
    @ApiModelProperty(value = "the location name of device", example = "br_sp", position = 5)
    protected String locationName;
    @ApiModelProperty(value = "the number of minutes that platform will wait for a new message until raising an alert", example = "2000", position = 6)
    protected int minutes;

}