package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

@Data
public class DeviceRegistrationForm implements ModelBuilder<Device,DeviceRegistrationForm> {

    private String deviceId;
    private String name;
    private String description;

    @Override
    public Device toModel() throws BusinessException {
        return Device.builder().deviceId(getDeviceId())
                .name(getName()).description(getDescription()).build();
    }

    @Override
    public DeviceRegistrationForm fillFrom(Device model) {
        this.setDeviceId(model.getDeviceId());
        this.setName(model.getName());
        this.setDescription(model.getDescription());
        return this;
    }
}
