package com.konkerlabs.platform.registry.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tenants")
@Data
@Builder
public class Tenant {

    @Id
    private String id;
    private String name;
}
