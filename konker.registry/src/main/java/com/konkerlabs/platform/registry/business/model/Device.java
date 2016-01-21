package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Document(collection = "devices")
public class Device {

    @DBRef
    private Tenant tenant;
    private String deviceId;
    private String name;
    private String description;
    private String registrationDate;
    private List<Event> events;

    public List<String> applyValidations() {

        List<String> validations = new ArrayList<>();

        if (getDeviceId() == null || getDeviceId().isEmpty())
            validations.add("Device id cannot be null or empty");
        if (getDeviceId() != null && getDeviceId().length() > 16)
            validations.add("Device cannot be greater than 16 characters");
        if (getName() == null || getName().isEmpty())
            validations.add("Device name cannot be null or empty");
        if (getTenant() == null)
            validations.add("Tenant cannot be null");

        if (validations.isEmpty())
            return null;
        else
            return validations;
    }
}
