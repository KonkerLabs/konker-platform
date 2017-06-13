package com.konkerlabs.platform.registry.business.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

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
