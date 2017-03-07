package com.konkerlabs.platform.registry.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
        value = "Device Input",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceInputVO {

    @ApiModelProperty(value = "the device id", example = "serialNumber", required = true, position = 1)
    protected String id;
    @ApiModelProperty(value = "the device name", example = "Device001", required = true, position = 2)
    protected String name;
    @ApiModelProperty(value = "the device description", example = "energy storage device", position = 3)
    protected String description;
    @ApiModelProperty(example = "true", position = 4)
    protected boolean active = true;

}
