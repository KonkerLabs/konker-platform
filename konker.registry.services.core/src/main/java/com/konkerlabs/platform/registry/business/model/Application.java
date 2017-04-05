package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "applications")
@Data
@Builder
public class Application {
    @Id
    private String id;
    private String guid;
    @DBRef
    private Tenant tenant;
    private String name;
    private String description;
    private Instant registrationDate;
    private Boolean active;
}
