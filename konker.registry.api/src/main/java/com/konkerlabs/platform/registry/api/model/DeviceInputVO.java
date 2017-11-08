package com.konkerlabs.platform.registry.api.model;

import java.util.Set;

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

    @ApiModelProperty(value = "the device id", example = "serialNumber", required = true, position = 1)
    protected String id;
    @ApiModelProperty(value = "the device name", example = "Device001", required = true, position = 2)
    protected String name;
    @ApiModelProperty(value = "the device description", example = "energy storage device", position = 3)
    protected String description;
    @ApiModelProperty(value = "the location name of device", example = "br_sp", position = 4)
    protected String locationName;
    @ApiModelProperty(value = "the device model name of device", example = "PresenceSensor", position = 5)
    protected String deviceModelName;
    @ApiModelProperty(example = "true", position = 6)
    protected boolean active = true;
    //LIST example = ["repair", "test1"] IS NOT SUPPORTED YET IN SWAGGER (08/11/2017)
    //@ApiModelProperty(value = "the tags of  the device", example = ["repair", "test1"], position = 7)
    @ApiModelProperty(value = "the tags of  the device", position = 7)
    protected Set<String> tags;

}
