package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

import java.util.function.Supplier;

@Data
public class DeviceRegistrationForm implements ModelBuilder<Device,DeviceRegistrationForm,Void> {

    private String deviceId;
    private String name;
    private String description;
    private boolean active;

    @Override
    public Device toModel() {
        return Device.builder()
                .deviceId(getDeviceId())
                .name(getName())
                .description(getDescription())
                .active(isActive())
                .build();
    }

    @Override
    public DeviceRegistrationForm fillFrom(Device model) {
        this.setDeviceId(model.getDeviceId());
        this.setName(model.getName());
        this.setDescription(model.getDescription());
        this.setActive(model.isActive());
        return this;
    }

    @Override
    public void setAdditionalSupplier(Supplier supplier) {
        //A supplier isn't necessary to this model builder
    }
}
