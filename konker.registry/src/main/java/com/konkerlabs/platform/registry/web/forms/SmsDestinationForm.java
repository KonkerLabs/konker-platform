package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

@Data
public class SmsDestinationForm implements ModelBuilder<SmsDestination, SmsDestinationForm, Void> {

    private String name;
    private String description;
    private String phoneNumber;
    private boolean active;

    @Override
    public SmsDestination toModel() {
        return SmsDestination.builder()
                .name(getName())
                .description(getDescription())
                .phoneNumber(getPhoneNumber())
                .active(isActive()).build();
    }

    @Override
    public SmsDestinationForm fillFrom(SmsDestination model) {
        setName(model.getName());
        setDescription(model.getDescription());
        setPhoneNumber(model.getPhoneNumber());
        setActive(model.isActive());
        return this;
    }

}
