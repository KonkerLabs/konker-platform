package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.business.model.Device;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(
        value = "Device",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceVO {

    @ApiModelProperty(value = "the device id")
    private String id;
    @ApiModelProperty(value = "the device guid")
    private String guid;
    @ApiModelProperty(value = "the device name")
    private String name;
    @ApiModelProperty(value = "the device description")
    private String description;
    
    public DeviceVO(Device device) {
        this.id   = device.getDeviceId();
        this.guid = device.getGuid();
        this.name = device.getName();
        this.description = device.getDescription();
    }
    
}
