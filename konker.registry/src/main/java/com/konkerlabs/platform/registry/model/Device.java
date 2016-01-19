package com.konkerlabs.platform.registry.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Device {

    private String id;
    private String name;
    private String description;
}
