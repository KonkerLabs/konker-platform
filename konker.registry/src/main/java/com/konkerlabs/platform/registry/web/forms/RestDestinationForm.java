package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

@Data
public class RestDestinationForm implements ModelBuilder<RestDestination, RestDestinationForm, Void> {

    private String name;
    private String serviceURI;
    private String serviceUsername;
    private String servicePassword;
    private boolean active;

    @Override
    public RestDestination toModel() {
        return RestDestination.builder()
            .name(getName())
            .serviceURI(getServiceURI())
            .serviceUsername(getServiceUsername())
            .servicePassword(getServicePassword())
            .active(isActive())
            .build();
    }

    @Override
    public RestDestinationForm fillFrom(RestDestination model) {
        setName(model.getName());
        setServiceURI(model.getServiceURI());
        setServiceUsername(model.getServiceUsername());
        setServicePassword(model.getServicePassword());
        setActive(model.isActive());
        return this;
    }

}
