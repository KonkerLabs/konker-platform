package com.konkerlabs.platform.registry.api.model;

import com.konkerlabs.platform.registry.business.model.Device;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceRest {

    private String id;
    private String guid;
    private String name;
    private String description;
    
    public DeviceRest(Device device) {
        this.id   = device.getDeviceId();
        this.guid = device.getGuid();
        this.name = device.getName();
        this.description = device.getDescription();
    }
    
}
