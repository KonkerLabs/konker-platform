package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@Document(collection = "restDestinations")
public class RestDestination implements RESTDestinationURIDealer {

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    private String guid;
    private String name;
    private URI serviceURI;
    private String serviceUsername;
    private String servicePassword;
    private boolean active;

    public List<String> applyValidations() {
        List<String> validations = new ArrayList<>();

        if (tenant == null) {
            validations.add("Tenant cannot be null");
        }

        if ("".equals(Optional.ofNullable(getName()).orElse("").trim())) {
            validations.add("Name cannot be null or empty");
        }

        if ("".equals(Optional.ofNullable(getServiceURI()).orElse(URI.create("")).toString().trim())) {
            validations.add("URL cannot be null or empty");
        }

        if (Optional.ofNullable(getServicePassword()).filter(s -> !s.isEmpty()).isPresent()) {
            if ("".equals(Optional.ofNullable(getServiceUsername()).orElse("").trim())) {
                validations.add("Password is set but username is empty");
            }
        }

        if (validations.isEmpty())
            return null;
        else
            return validations;
    }

    public URI toURI() {
        return toRestDestinationURI(Optional.ofNullable(tenant).map(Tenant::getDomainName).orElse(""), this.getName());
    }
}
