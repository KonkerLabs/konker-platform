package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@Document(collection = "smsDestinations")
public class SmsDestination implements SmsDestinationURIDealer {

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    private String name;
    private String description;
    private String phoneNumber;
    private boolean active;
    private String guid;

    public List<String> applyValidations() {
        List<String> validations = new ArrayList<>();

        if (!Optional.ofNullable(getTenant()).isPresent())
            validations.add("Tenant cannot be null");
        if (!Optional.ofNullable(getName()).filter(s -> !s.isEmpty()).isPresent())
            validations.add("Name cannot be null or empty");
        if (!Optional.ofNullable(getPhoneNumber()).filter(s -> !s.isEmpty()).isPresent())
            validations.add("Phone number cannot be null or empty");

        Optional.ofNullable(getGuid())
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalStateException("GUID cannot be null or empty"));

        return validations;
    }

    public URI toURI() {
        return toSmsURI(Optional.ofNullable(getTenant()).orElse(Tenant.builder().build()).getDomainName(),getGuid());
    }
}
