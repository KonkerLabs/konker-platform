package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.api.model.core.SerializableVO;
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
public class DeviceVO extends DeviceInputVO
        implements SerializableVO<Device, DeviceVO> {

    @ApiModelProperty(value = "the device guid", position = 0)
    private String guid;

    public DeviceVO(Device device) {
        this.id     = device.getDeviceId();
        this.guid   = device.getGuid();
        this.name   = device.getName();
        this.description = device.getDescription();
        this.active = device.isActive();
    }

    @Override
    public DeviceVO apply(Device t) {
        this.setGuid(t.getGuid());
        this.setName(t.getName());
        this.setDescription(t.getDescription());
        this.setActive(t.isActive());
        this.setId(t.getId());
        return this;
    }

    @Override
    public Device applyDB(Device t) {
        t.setActive(this.isActive());
        t.setName(this.getName());
        t.setDescription(this.getDescription());
        t.setId(this.getId());
        t.setGuid(this.getGuid());
        return t;
    }
}
