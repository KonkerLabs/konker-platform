package com.konkerlabs.platform.registry.business.model.destinations;


import com.konkerlabs.platform.registry.business.model.Tenant;
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
@Document(collection = "outgoingRest")
public class RestDestination {

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    private String name;
    private URI uri;
    private String username;
    private String password;
    private boolean active;

    public List<String> applyValidations() {
        List<String> validations = new ArrayList<>();

        if (!Optional.ofNullable(getTenant()).isPresent())
            validations.add("Tenant cannot be null");
        if (!Optional.ofNullable(getName()).filter(s -> !s.isEmpty()).isPresent())
            validations.add("Name cannot be null or empty");
        if (!Optional.ofNullable(getUri()).filter(s -> !s.toString().isEmpty()).isPresent())
            validations.add("URL cannot be null or empty");
//        if (!Optional.ofNullable(getUsername()).filter(s -> !s.isEmpty()).isPresent())
//            validations.add("Username cannot be null or empty");
//        if (!Optional.ofNullable(getPassword()).filter(s -> !s.isEmpty()).isPresent())
//            validations.add("Password cannot be null or empty");

        return validations;
    }
}
