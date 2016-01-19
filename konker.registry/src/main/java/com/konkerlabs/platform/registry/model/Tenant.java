package com.konkerlabs.platform.registry.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Tenant {

    private String name;
}
