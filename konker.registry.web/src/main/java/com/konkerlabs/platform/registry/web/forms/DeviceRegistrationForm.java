package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

@Data
public class DeviceRegistrationForm implements ModelBuilder<Device,DeviceRegistrationForm,Void> {

    private String deviceId;
    private String name;
    private String description;
    private String guid;
    private boolean active;
    
    public DeviceRegistrationForm() {
		setActive(Boolean.TRUE);
	}

    @Override
    public Device toModel() {
        return Device.builder()
                .deviceId(getDeviceId())
                .name(getName())
                .description(getDescription())
                .guid(getGuid())
                .active(isActive())
                .build();
    }

    @Override
    public DeviceRegistrationForm fillFrom(Device model) {
        this.setDeviceId(model.getDeviceId());
        this.setName(model.getName());
        this.setDescription(model.getDescription());
        this.setActive(model.isActive());
        this.setGuid(model.getGuid());
        return this;
    }
}
