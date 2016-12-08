package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

@Data
public class UserRegistrationForm implements ModelBuilder<Device,UserRegistrationForm,Void> {

    private String email;
    private String name;
    private String password;
    private String phone;
    private String 


    public UserRegistrationForm() {
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
    public UserRegistrationForm fillFrom(Device model) {
        this.setDeviceId(model.getDeviceId());
        this.setName(model.getName());
        this.setDescription(model.getDescription());
        this.setActive(model.isActive());
        this.setGuid(model.getGuid());
        return this;
    }
}
