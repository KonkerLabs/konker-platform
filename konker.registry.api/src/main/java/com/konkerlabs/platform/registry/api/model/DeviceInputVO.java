package com.konkerlabs.platform.registry.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
        value = "Device Input",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceInputVO {

    @ApiModelProperty(value = "the device id", position = 1)
    protected String id;
    @ApiModelProperty(value = "the device name", position = 2)
    protected String name;
    @ApiModelProperty(value = "the device description", position = 3)
    protected String description;
    @ApiModelProperty(position = 4)
    protected boolean active = true;

}
