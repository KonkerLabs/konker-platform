package com.konkerlabs.platform.registry.business.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document(collection = "deviceConfigSetups")
public class DeviceConfigSetup implements Comparable<DeviceConfigSetup> {

    @Id
    private String id;
    @DBRef
    private Tenant tenant;
    @DBRef
    private Application application;
    private int version;
    private Instant date;
    private List<DeviceConfig> configs;

    @Override
    public int compareTo(DeviceConfigSetup o) {
        return -Integer.compare(version, o.version);
    }

}
