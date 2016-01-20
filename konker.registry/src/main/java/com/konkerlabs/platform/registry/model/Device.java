package com.konkerlabs.platform.registry.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private List<Event> events;
}
