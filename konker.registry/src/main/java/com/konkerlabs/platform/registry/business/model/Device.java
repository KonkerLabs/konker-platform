package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
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
    private Instant registrationDate;
    private List<Event> events;

    public List<String> applyValidations() {

        List<String> validations = new ArrayList<>();

        if (getDeviceId() == null || getDeviceId().isEmpty())
            validations.add("Device ID cannot be null or empty");
        if (getDeviceId() != null && getDeviceId().length() > 16)
            validations.add("Device ID cannot be greater than 16 characters");
        if (getName() == null || getName().isEmpty())
            validations.add("Device name cannot be null or empty");
        if (getTenant() == null)
            validations.add("Tenant cannot be null");
        if (getRegistrationDate() == null)
            validations.add("Registration date cannot be null");

        if (validations.isEmpty())
            return null;
        else
            return validations;
    }

    public void onRegistration() {
        setRegistrationDate(Instant.now());
    }
}
