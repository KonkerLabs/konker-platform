package com.konkerlabs.platform.registry.business.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document(collection = "applicationDocumentsStore")
public class ApplicationDocumentStore {

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    @DBRef
    private Application application;
    private String collection;
    private String key;
    private String json;
    private Instant lastChange;

    public Optional<Map<String, Object[]>> applyValidations() {
        Map<String, Object[]> validations = new HashMap<>();

        return Optional.of(validations).filter(map -> !map.isEmpty());
    }

}
