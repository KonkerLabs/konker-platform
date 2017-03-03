package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.business.model.Device;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ApiModel(
        value = "Device",
        discriminator = "com.konkerlabs.platform.registry.api.model")
public class DeviceVO extends DeviceInputVO {

    @ApiModelProperty(value = "the device guid", position = 0)
    private String guid;

    public DeviceVO(Device device) {
        this.id     = device.getDeviceId();
        this.guid   = device.getGuid();
        this.name   = device.getName();
        this.description = device.getDescription();
        this.active = device.isActive();
    }

}
